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

package com.navercorp.pinpoint.collector.metric.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.collector.metric.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.collector.metric.dao.pinot.PinotSystemMetricLongDao;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricService {
    private final PinotSystemMetricLongDao pinotSystemMetricLongDao;
    private final PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao;
    private final SystemMetricMetadata systemMetricMetadata;

    public SystemMetricService(PinotSystemMetricLongDao pinotSystemMetricLongDao,
                               PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao) {
        this.pinotSystemMetricLongDao = Objects.requireNonNull(pinotSystemMetricLongDao, "pinotSystemMetricLongDao");
        this.pinotSystemMetricDoubleDao = Objects.requireNonNull(pinotSystemMetricDoubleDao, "pinotSystemMetricDoubleDao");
        this.systemMetricMetadata = SystemMetricMetadata.getMetadata();
    }

    public void insert(String applicationName, List<SystemMetricBo> systemMetricBoList) throws JsonProcessingException {
        Map<SystemMetricMetadata.MetricType, List<SystemMetricBo>> groupedSystemMetricBos = groupSystemMetric(systemMetricBoList);
        pinotSystemMetricLongDao.insert(applicationName, groupedSystemMetricBos.get(SystemMetricMetadata.MetricType.LongCounter));
        pinotSystemMetricDoubleDao.insert(applicationName, groupedSystemMetricBos.get(SystemMetricMetadata.MetricType.DoubleCounter));
    }

    public Map<SystemMetricMetadata.MetricType, List<SystemMetricBo>> groupSystemMetric (List<SystemMetricBo> systemMetricBos) {
        return systemMetricBos.stream().collect(Collectors.groupingBy(metric -> systemMetricMetadata.get(metric.getMetricName(), metric.getFieldName())));
    }
}
