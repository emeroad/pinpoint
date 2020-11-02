package com.navercorp.pinpoint.web.metric.mapper;

import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;

import java.util.List;
import java.util.Map;

public interface SystemMetricMapper<T extends Number> {
    List<SystemMetricBo> mapSystemMetricBo(List<Map<String, String>> resultMap, String metricName, String fieldName);
    List<SampledSystemMetric<T>> mapSampledSystemMetric(List<Map<String, String>> resultMap);
}
