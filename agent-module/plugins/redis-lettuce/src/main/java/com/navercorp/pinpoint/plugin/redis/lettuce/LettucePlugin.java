/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.lettuce;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParameters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallbackParametersBuilder;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;


import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.AttachEndPointInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.LettuceMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisCluserClientConnectStatefulInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClusterClientConstructorInterceptor;

import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisPubSubListenerInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class LettucePlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final LettucePluginConfig config = new LettucePluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        }

        // Set endpoint
        addRedisClient();
        // Attach endpoint
        addStatefulRedisConnection();
        // Commands
        addRedisCommands(config);
        addReactive();
        if (config.isTracePubSubListener()) {
            addRedisPubSubListener(config);
        }
    }

    private void addRedisClient() {
        transformTemplate.transform("io.lettuce.core.RedisClient", RedisClientTransform.class);
        transformTemplate.transform("io.lettuce.core.cluster.RedisClusterClient", RedisClientTransform.class);
    }

    public static class RedisClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);

            // Set endpoint
            final InstrumentMethod constructor = target.getConstructor("io.lettuce.core.resource.ClientResources", "io.lettuce.core.RedisURI");
            if (constructor != null) {
                constructor.addInterceptor(RedisClientConstructorInterceptor.class);
            }

            // Set cluster endpoint
            final InstrumentMethod clusterConstructor = target.getConstructor("io.lettuce.core.resource.ClientResources", "java.lang.Iterable");
            if (clusterConstructor != null) {
                clusterConstructor.addInterceptor(RedisClusterClientConstructorInterceptor.class);
            }

            // 5.0
            InstrumentMethod newStatefulRedisConnectionMethod = target.getDeclaredMethod("newStatefulRedisConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisConnectionMethod == null) {
                // 6.x
                newStatefulRedisConnectionMethod = target.getDeclaredMethod("newStatefulRedisConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.protocol.PushHandler", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            }
            if (newStatefulRedisConnectionMethod != null) {
                newStatefulRedisConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }
            final InstrumentMethod newStatefulRedisPubSubConnectionMethod = target.getDeclaredMethod("newStatefulRedisPubSubConnection", "io.lettuce.core.pubsub.PubSubEndpoint", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisPubSubConnectionMethod != null) {
                newStatefulRedisPubSubConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }
            final InstrumentMethod newStatefulRedisSentinelConnectionMethod = target.getDeclaredMethod("newStatefulRedisSentinelConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisSentinelConnectionMethod != null) {
                newStatefulRedisSentinelConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }

            // Cluster
            // 5.0
            // private <K, V, T extends RedisChannelHandler<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Supplier<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
            InstrumentMethod connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.RedisChannelHandler", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "java.util.function.Supplier", "java.util.function.Supplier");
            if (connectStatefulAsyncMethod == null) {
                // 5.1
                // private <K, V, T extends RedisChannelHandler<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, RedisCodec<K, V> codec, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.RedisChannelHandler", "io.lettuce.core.codec.RedisCodec", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod == null) {
                // 6.0, StatefulRedisConnectionImpl
                // private <K, V, T extends StatefulRedisConnectionImpl<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.StatefulRedisConnectionImpl", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod == null) {
                // 6.0, StatefulRedisClusterConnectionImpl
                // private <K, V, T extends StatefulRedisClusterConnectionImpl<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod != null) {
                connectStatefulAsyncMethod.addInterceptor(RedisCluserClientConnectStatefulInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addStatefulRedisConnection() {
        addStatefulRedisConnection("io.lettuce.core.StatefulRedisConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.sentinel.StatefulRedisSentinelConnectionImpl");

        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisPubSubConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisClusterPubSubConnectionImpl");
    }

    private void addStatefulRedisConnection(final String className) {
        transformTemplate.transform(className, AddEndPointAccessorTransform.class);
    }

    public static class AddEndPointAccessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);
            return target.toBytecode();
        }
    }

    private void addRedisCommands(final LettucePluginConfig config) {
        // Commands
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisAsyncCommands", AbstractRedisCommandsTransform.class, true);

        addAbstractRedisCommands("io.lettuce.core.RedisAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);

        // Reactive
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisReactiveCommands", AbstractRedisCommandsTransform.class, true);

        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.RedisReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.sentinel.RedisSentinelReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
    }

    private void addAbstractRedisCommands(final String className, Class<? extends TransformCallback> transformCallback, boolean getter) {
        TransformCallbackParameters parameters = TransformCallbackParametersBuilder.newBuilder()
                .addBoolean(getter)
                .build();
        transformTemplate.transform(className, transformCallback, parameters);
    }

    public static class AbstractRedisCommandsTransform implements TransformCallback {
        private final boolean getter;

        public AbstractRedisCommandsTransform(Boolean getter) {
            this.getter = getter;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            if (getter) {
                target.addGetter(StatefulConnectionGetter.class, "connection");
            }
            final LettuceMethodNameFilter lettuceMethodNameFilter = new LettuceMethodNameFilter();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(lettuceMethodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                try {
                    method.addScopedInterceptor(LettuceMethodInterceptor.class, LettuceConstants.REDIS_SCOPE);
                } catch (Exception e) {
                    final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method {}", method, e);
                    }
                }
            }
            return target.toBytecode();
        }
    }

    private void addReactive() {
        transformTemplate.transform("io.lettuce.core.RedisPublisher", RedisPublisherTransform.class);
    }

    public static class RedisPublisherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addRedisPubSubListener(LettucePluginConfig config) {
        final Matcher listenerMatcher = Matchers.newPackageBasedMatcher("io.lettuce.core.pubsub.RedisPubSubReactiveCommandsImpl$", new InterfaceInternalNameMatcherOperand("io.lettuce.core.pubsub.RedisPubSubListener", true));
        transformTemplate.transform(listenerMatcher, RedisPubSubListenerTransform.class);

        for (String packageName : config.getRedisPubSubListenerBasePackageList()) {
            Matcher matcher = Matchers.newPackageBasedMatcher(packageName, new InterfaceInternalNameMatcherOperand("io.lettuce.core.pubsub.RedisPubSubListener", true));
            transformTemplate.transform(matcher, RedisPubSubListenerTransform.class);
        }
    }

    public static class RedisPubSubListenerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // void message(K channel, V message);
            final InstrumentMethod messageMethod1 = target.getDeclaredMethod("message", "java.lang.Object", "java.lang.Object");
            if (messageMethod1 != null) {
                messageMethod1.addInterceptor(RedisPubSubListenerInterceptor.class);
            }
            // void message(K pattern, K channel, V message);
            final InstrumentMethod messageMethod2 = target.getDeclaredMethod("message", "java.lang.Object", "java.lang.Object", "java.lang.Object");
            if (messageMethod2 != null) {
                messageMethod2.addInterceptor(RedisPubSubListenerInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}