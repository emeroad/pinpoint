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

package com.navercorp.pinpoint.web.metric.mapper.pinot;

import com.navercorp.pinpoint.common.server.metric.model.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.model.Tag;
import com.navercorp.pinpoint.web.metric.mapper.SystemMetricMapper;
import com.navercorp.pinpoint.web.metric.util.SystemMetricUtils;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import com.navercorp.pinpoint.web.metric.vo.chart.SystemMetricPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Hyunjoon Cho
 */
@Component
public class PinotSystemMetricLongMapper implements SystemMetricMapper<Long> {
    @Override
    public List<SystemMetricBo> mapSystemMetricBo(List<Map<String, String>> resultMap, String metricName, String fieldName) {
        List<SystemMetricBo> systemMetricBoList = new ArrayList<>();

        for (Map<String, String> result : resultMap) {
            long fieldValue = Long.parseLong(result.get("fieldValue"));
            List<Tag> tagList = SystemMetricUtils.parseTagBos(result.get("tagName"), result.get("tagValue"));
            long timestamp = Long.parseLong(result.get("timestampInEpoch"));

            systemMetricBoList.add(new SystemMetricBo<>(metricName, fieldName, fieldValue, tagList, timestamp));
        }

        return systemMetricBoList;
    }

    @Override
    public List<SampledSystemMetric<Long>> mapSampledSystemMetric(List<Map<String, String>> resultMap) {
        List<SampledSystemMetric<Long>> sampledSystemMetricList = new ArrayList<>();

        for (Map<String, String> result : resultMap) {
            long fieldValue = Long.parseLong(result.get("fieldValue"));
            long timestamp = Long.parseLong(result.get("timestampInEpoch"));
            SystemMetricPoint<Long> systemMetricPoint = new SystemMetricPoint<>(timestamp, fieldValue);
            List<Tag> tagList = SystemMetricUtils.parseTagBos(result.get("tagName"), result.get("tagValue"));

            sampledSystemMetricList.add(new SampledSystemMetric<>(systemMetricPoint, tagList.toString()));
        }

        return sampledSystemMetricList;
    }
}
