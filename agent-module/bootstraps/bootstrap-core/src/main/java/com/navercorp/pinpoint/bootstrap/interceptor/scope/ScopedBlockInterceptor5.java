/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor.scope;

import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

public class ScopedBlockInterceptor5 implements BlockAroundInterceptor5 {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final BlockAroundInterceptor5 interceptor;
    private final InterceptorScope scope;
    private final ExecutionPolicy policy;

    public ScopedBlockInterceptor5(BlockAroundInterceptor5 interceptor, InterceptorScope scope, ExecutionPolicy policy) {
        this.interceptor = Objects.requireNonNull(interceptor, "interceptor");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    @Override
    public TraceBlock before(Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.tryEnter(policy)) {
            return interceptor.before(target, arg0, arg1, arg2, arg3, arg4);
        } else {
            if (debugEnabled) {
                logger.debug("tryBefore() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, interceptor.getClass());
            }
        }

        return null;
    }

    @Override
    public void after(TraceBlock block, Object target, Object arg0, Object arg1, Object arg2, Object arg3, Object arg4, Object result, Throwable throwable) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.canLeave(policy)) {
            try {
                interceptor.after(block, target, arg0, arg1, arg2, arg3, arg4, result, throwable);
            } finally {
                transaction.leave(policy);
            }
        } else {
            if (debugEnabled) {
                logger.debug("tryAfter() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, interceptor.getClass());
            }
        }
    }
}
