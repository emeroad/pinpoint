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

package com.navercorp.pinpoint.web.metric.service;

import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricMetadata;
import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import com.navercorp.pinpoint.web.metric.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.web.metric.dao.pinot.PinotSystemMetricLongDao;
import com.navercorp.pinpoint.web.metric.util.SystemMetricUtils;
import com.navercorp.pinpoint.web.metric.vo.QueryParameter;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import com.navercorp.pinpoint.web.metric.vo.chart.SystemMetricChart;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PinotSystemMetricLongDao pinotSystemMetricLongDao;
    private final PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao;
    private final SystemMetricMetadata systemMetricMetadata;

    public SystemMetricService(PinotSystemMetricLongDao pinotSystemMetricLongDao,
                               PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao) {
        this.pinotSystemMetricLongDao = Objects.requireNonNull(pinotSystemMetricLongDao, "pinotSystemMetricLongDao");
        this.pinotSystemMetricDoubleDao = Objects.requireNonNull(pinotSystemMetricDoubleDao, "pinotSystemMetricDoubleDao");
        this.systemMetricMetadata = SystemMetricMetadata.getMetadata();
    }

    public List<SystemMetricBo> getSystemMetricBoList(String applicationName, String metricName, String fieldName, List<String> tags, Range range){
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(metricName, "metricName");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(tags, "tags");

        List<TagBo> tagBoList = SystemMetricUtils.parseTagBos(tags);

        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setApplicationName(applicationName);
        queryParameter.setMetricName(metricName);
        queryParameter.setFieldName(fieldName);
        queryParameter.setTagBoList(tagBoList);
        queryParameter.setRange(range);

        SystemMetricMetadata.MetricType metricType = systemMetricMetadata.get(metricName, fieldName);

        switch (metricType) {
            case LongCounter:
                return pinotSystemMetricLongDao.getSystemMetricBo(queryParameter);
            case DoubleCounter:
                return pinotSystemMetricDoubleDao.getSystemMetricBo(queryParameter);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }

    public SystemMetricChart getSystemMetricChart(String applicationName, String metricName, String fieldName, List<String> tags, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(metricName, "metricName");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(tags, "tags");

        List<TagBo> tagBoList = SystemMetricUtils.parseTagBos(tags);

        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setApplicationName(applicationName);
        queryParameter.setMetricName(metricName);
        queryParameter.setFieldName(fieldName);
        queryParameter.setTagBoList(tagBoList);
        queryParameter.setRange(timeWindow.getWindowRange());

        long intervalMs = timeWindow.getWindowSlotSize();
        if (intervalMs != 10000) {
            queryParameter.setIntervalMs(intervalMs);
        }

        SystemMetricMetadata.MetricType metricType = systemMetricMetadata.get(metricName, fieldName);
        String chartName = metricName.concat("_").concat(fieldName);

        switch (metricType) {
            case LongCounter:
                List<SampledSystemMetric<Long>> sampledLongSystemMetrics = pinotSystemMetricLongDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart(timeWindow, chartName, sampledLongSystemMetrics);
            case DoubleCounter:
                List<SampledSystemMetric<Double>> sampledDoubleSystemMetrics = pinotSystemMetricDoubleDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart(timeWindow, chartName, sampledDoubleSystemMetrics);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }
}
