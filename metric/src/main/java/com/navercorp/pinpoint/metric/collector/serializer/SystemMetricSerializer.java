package com.navercorp.pinpoint.metric.collector.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.metric.common.model.SystemMetricBo;

import java.util.List;

public interface SystemMetricSerializer {
    List<String> serialize(String applicationName, List<SystemMetricBo> systemMetricBos) throws JsonProcessingException;
}
