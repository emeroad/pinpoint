/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.okhttp.v2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseHeaderRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServerResponseHeaderRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpPluginConfig;
import com.navercorp.pinpoint.plugin.okhttp.v2.OkHttpResponseAdaptor;
import com.navercorp.pinpoint.plugin.okhttp.v2.UserRequestGetter;
import com.navercorp.pinpoint.plugin.okhttp.v2.UserResponseGetter;
import com.squareup.okhttp.Response;

/**
 * @author jaehong.kim
 */
public class HttpEngineReadResponseMethodInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final boolean statusCode;
    private final ServerResponseHeaderRecorder<Response> responseHeaderRecorder;
    private final boolean markError;

    public HttpEngineReadResponseMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.statusCode = OkHttpPluginConfig.isStatusCode(traceContext.getProfilerConfig());
        this.responseHeaderRecorder = ResponseHeaderRecorderFactory.newResponseHeaderRecorder(traceContext.getProfilerConfig(), new OkHttpResponseAdaptor());
        this.markError = OkHttpPluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!validate(target)) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL);
    }

    private boolean validate(Object target) {
        if (!(target instanceof UserRequestGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_REQUEST);
            }
            return false;
        }

        if (!(target instanceof UserResponseGetter)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", OkHttpConstants.FIELD_USER_RESPONSE);
            }
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!validate(target)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(markError, throwable);

            if (statusCode) {
                // type check validate();
                final Response response = ((UserResponseGetter) target)._$PINPOINT$_getUserResponse();
                if (response != null) {
                    recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, response.code());
                    this.responseHeaderRecorder.recordHeader(recorder, response);
                }
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}