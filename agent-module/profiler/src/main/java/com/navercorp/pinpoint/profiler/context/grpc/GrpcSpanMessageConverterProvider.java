/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapper;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.profiler.name.ObjectName;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcSpanMessageConverterProvider implements Provider<MessageConverter<SpanType, GeneratedMessageV3>> {

    private final ObjectName objectName;
    private final short applicationServiceTypeCode;
    private final SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor;
    private final SpanMessageMapper mapper;

    @Inject
    public GrpcSpanMessageConverterProvider(ObjectName objectName,
                                            @ApplicationServerType ServiceType applicationServiceType,
                                            SpanProcessor<PSpan.Builder, PSpanChunk.Builder> spanPostProcessor,
                                            SpanMessageMapper spanMessageMapper) {
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.applicationServiceTypeCode = applicationServiceType.getCode();
        this.spanPostProcessor = Objects.requireNonNull(spanPostProcessor, "spanPostProcessor");
        this.mapper = Objects.requireNonNull(spanMessageMapper, "spanMessageMapper");
    }

    @Override
    public MessageConverter<SpanType, GeneratedMessageV3> get() {
        return new GrpcSpanMessageConverter(objectName.getAgentId(), applicationServiceTypeCode, spanPostProcessor, mapper);
    }

}
