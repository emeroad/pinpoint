/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client.config;

import com.navercorp.pinpoint.common.config.util.ValueAnnotationProcessor;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ClientOptionTest {

    @Test
    public void build() {
        Properties properties = new Properties();
        properties.setProperty("keepalive.time.millis", "1");
        properties.setProperty("keepalive.timeout.millis", "2");

        properties.setProperty("flow-control.window.size", "65535");
        properties.setProperty("headers.size.max", "4");

        properties.setProperty("message.inbound.size.max", "5");
        properties.setProperty("connect.timeout.millis", "6");

        properties.setProperty("write.buffer.highwatermark", "7");
        properties.setProperty("write.buffer.lowwatermark", "8");


        ClientOption clientOption = new ClientOption();
        ValueAnnotationProcessor processor = new ValueAnnotationProcessor();
        processor.process(clientOption, properties::getProperty);

        assertEquals(1, clientOption.getKeepAliveTime());
        assertEquals(2, clientOption.getKeepAliveTimeout());
        assertFalse(clientOption.isKeepAliveWithoutCalls());
        assertEquals(TimeUnit.DAYS.toMillis(30), clientOption.getIdleTimeoutMillis());
        assertEquals(65535, clientOption.getFlowControlWindow());
        assertEquals(4, clientOption.getMaxHeaderListSize());

        assertEquals(5, clientOption.getMaxInboundMessageSize());
        assertEquals(6, clientOption.getConnectTimeout());

        assertEquals(7, clientOption.getWriteBufferHighWaterMark());
        assertEquals(8, clientOption.getWriteBufferLowWaterMark());
    }
}