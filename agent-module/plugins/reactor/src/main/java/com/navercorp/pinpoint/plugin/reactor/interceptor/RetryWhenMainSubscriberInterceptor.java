/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

public class RetryWhenMainSubscriberInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {

    private final boolean traceRetry;
    private final boolean markErrorRetry;

    public RetryWhenMainSubscriberInterceptor(TraceContext traceContext) {
        super(traceContext);
        final ReactorPluginConfig config = new ReactorPluginConfig(traceContext.getProfilerConfig());
        this.traceRetry = config.isTraceRetry();
        this.markErrorRetry = config.isMarkErrorRetry();
    }

    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (traceRetry) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
    }

    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (traceRetry) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (traceRetry && trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordServiceType(ReactorConstants.REACTOR);

            final Throwable argThrowable = ArrayArgumentUtils.getArgument(args, 0, Throwable.class);
            if (argThrowable != null) {
                recorder.recordException(markErrorRetry, argThrowable);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}
