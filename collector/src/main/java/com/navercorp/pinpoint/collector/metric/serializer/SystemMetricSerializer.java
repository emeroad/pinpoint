package com.navercorp.pinpoint.collector.metric.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;

import java.util.List;

public interface SystemMetricSerializer {
    List<String> serialize(String applicationName, List<SystemMetricBo> systemMetricBos) throws JsonProcessingException;
}
