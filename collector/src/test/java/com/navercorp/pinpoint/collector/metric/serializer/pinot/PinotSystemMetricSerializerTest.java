/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.collector.metric.serializer.pinot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.collector.metric.serializer.SystemMetricSerializer;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class PinotSystemMetricSerializerTest {
    @Test
    public void serializerTest() throws JsonProcessingException {
        List<SystemMetricBo> systemMetricBos = new ArrayList<>();
//        FieldBo fieldBo = createFieldBo();
//        systemMetricBos.add(new SystemMetricBo("cpu", fieldBo, createTagBoList(0), 0L));
//        systemMetricBos.add(new SystemMetricBo("cpu", fieldBo, createTagBoList(1), 0L));

        SystemMetricSerializer serializer = new PinotSystemMetricLongSerializer();
        List<String> serializedMetric = serializer.serialize("hyunjoon", systemMetricBos);
        for (String metric : serializedMetric) {
            System.out.println(metric);
        }
    }

//    public FieldBo createFieldBo() {
//        return new FieldBo("usage_user", 3.31F);
//    }

    public List<TagBo> createTagBoList(int num) {
        List<TagBo> tagBos = new ArrayList<>();
        tagBos.add(new TagBo("cpu", "cpu" + num));
        tagBos.add(new TagBo("host", "localhost"));

        return tagBos;
    }
}
