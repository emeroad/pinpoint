/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.metric.dao.pinot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.collector.metric.dao.SystemMetricDao;
import com.navercorp.pinpoint.collector.metric.serializer.pinot.PinotSystemMetricLongSerializer;
import com.navercorp.pinpoint.collector.metric.util.SystemMetricTemplate;
import com.navercorp.pinpoint.collector.metric.util.pinot.PinotKafkaLongProducer;
import com.navercorp.pinpoint.common.server.metric.model.SystemMetricBo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricLongDao implements SystemMetricDao {
    private final PinotSystemMetricLongSerializer pinotSystemMetricLongSerializer;
    private final PinotKafkaLongProducer pinotKafkaLongProducer;
    private final SystemMetricTemplate systemMetricTemplate;

    public PinotSystemMetricLongDao(PinotSystemMetricLongSerializer pinotSystemMetricLongSerializer,
                                    PinotKafkaLongProducer pinotKafkaLongProducer,
                                    SystemMetricTemplate systemMetricTemplate) {
        this.pinotSystemMetricLongSerializer = Objects.requireNonNull(pinotSystemMetricLongSerializer, "pinotSystemMetricLongSerializer");
        this.pinotKafkaLongProducer = Objects.requireNonNull(pinotKafkaLongProducer, "pinotKafkaLongProducer");
        this.systemMetricTemplate = Objects.requireNonNull(systemMetricTemplate, "systemMetricTemplate");
    }
    @Override
    public void insert(String applicationName, List<SystemMetricBo> systemMetricBos) throws JsonProcessingException{
        systemMetricTemplate.saveMetric(applicationName, systemMetricBos, pinotSystemMetricLongSerializer, pinotKafkaLongProducer);
    }
}
