/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.filter.SequenceSpanEventFilter;
import com.navercorp.pinpoint.common.server.bo.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.SpanVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorGrpcSpanFactoryTest {
    private static final int MAX_SEQUENCE = 10;

    private final GrpcSpanBinder binder = new GrpcSpanBinder();
    private final SpanEventFilter filter = new SequenceSpanEventFilter(MAX_SEQUENCE);

    private final BindAttribute attribute = newAttribute();

    private BindAttribute newAttribute() {
        return new BindAttribute("agentId", "agentName", "applicationName", () -> ApplicationUid.of(100), 88, System.currentTimeMillis());
    }

    private final GrpcSpanFactory factory = new CollectorGrpcSpanFactory(binder, filter);

    @Test
    public void buildSpanChunkBo_sequence_overflow_NPE() {
        final PSpanChunk chunk = newSpanChunk_overflow();

        SpanChunkBo spanChunkBo = factory.buildSpanChunkBo(chunk, attribute);

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        Assertions.assertTrue(spanEventBoList.isEmpty());
    }

    @Test
    public void buildSpanChunkBo_compact_depth() {
        final PSpanChunk chunk = newSpanChunk_compact_depth();

        SpanChunkBo spanChunkBo = factory.buildSpanChunkBo(chunk, attribute);

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();

        SpanEventBo spanEventBo0 = spanEventBoList.get(0);
        SpanEventBo spanEventBo1 = spanEventBoList.get(1);
        Assertions.assertEquals(1, spanEventBo0.getDepth());
        Assertions.assertEquals(1, spanEventBo1.getDepth());
    }

    @Test
    public void buildSpanChunkBo_compact_depth_NPE() {
        final PSpanChunk chunk = newSpanChunk_compact_depth_error();

        SpanChunkBo spanChunkBo = factory.buildSpanChunkBo(chunk, attribute);

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        SpanEventBo spanEventBo0 = spanEventBoList.get(0);
        Assertions.assertEquals(1, spanEventBo0.getDepth());
    }

    @Test
    public void buildSpanChunkBo_first_depth_zero() {
        final PSpanChunk chunk = newSpanChunk_first_depth_zero();

        SpanChunkBo spanChunkBo = factory.buildSpanChunkBo(chunk, attribute);

        List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        SpanEventBo spanEventBo0 = spanEventBoList.get(0);
        SpanEventBo spanEventBo1 = spanEventBoList.get(1);
        Assertions.assertEquals(0, spanEventBo0.getDepth());
        Assertions.assertEquals(1, spanEventBo1.getDepth());
    }


    private PSpanChunk newSpanChunk_overflow() {
        PSpanEvent sequenceOverflowEvent = newEvent(MAX_SEQUENCE + 1, 1);
        PSpanEvent event = newEvent(MAX_SEQUENCE + 2, 0);

        return newSpanChunk(sequenceOverflowEvent, event);
    }

    private PSpanChunk newSpanChunk_compact_depth() {
        PSpanEvent sequenceOverflowEvent = newEvent(1, 1);
        PSpanEvent event = newEvent(2, 0);

        return newSpanChunk(sequenceOverflowEvent, event);
    }

    private PSpanChunk newSpanChunk_compact_depth_error() {
        PSpanEvent sequenceOverflowEvent = newEvent(MAX_SEQUENCE + 1, 1);
        PSpanEvent event = newEvent(1, 0);

        return newSpanChunk(sequenceOverflowEvent, event);
    }

    private PSpanChunk newSpanChunk_first_depth_zero() {
        PSpanEvent sequenceOverflowEvent = newEvent(1, 0);
        PSpanEvent event = newEvent(2, 1);

        return newSpanChunk(sequenceOverflowEvent, event);
    }

    private PSpanChunk newSpanChunk() {
        PSpanEvent event0 = newEvent(1, 1);
        PSpanEvent event1 = newEvent(2, 0);

        return newSpanChunk(event0, event1);
    }

    private PSpanChunk newSpanChunk(PSpanEvent... events) {
        PSpanChunk.Builder builder = PSpanChunk.newBuilder();
        builder.setVersion(SpanVersion.TRACE_V2);
        builder.addAllSpanEvent(List.of(events));
        builder.setTransactionId(PTransactionId.getDefaultInstance());
        return builder.build();
    }


    private PSpanEvent newEvent(int sequence, int depth) {
        PSpanEvent.Builder event = PSpanEvent.newBuilder();
        event.setSequence(sequence);
        event.setDepth(depth);
        return event.build();
    }


}