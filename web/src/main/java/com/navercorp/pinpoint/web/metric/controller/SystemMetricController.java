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

package com.navercorp.pinpoint.web.metric.controller;

import com.navercorp.pinpoint.common.server.metric.model.SystemMetricBo;
import com.navercorp.pinpoint.web.metric.service.SystemMetricService;
import com.navercorp.pinpoint.web.metric.vo.chart.SystemMetricChart;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Controller
@RequestMapping(value = "/systemMetric")
public class SystemMetricController {
    private final SystemMetricService systemMetricService;

    public SystemMetricController(SystemMetricService systemMetricService) {
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
    }

    @RequestMapping(value = "/systemMetricBoList")
    @ResponseBody
    public List<SystemMetricBo> getSystemMetricBoList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("tags") List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to){
        return systemMetricService.getSystemMetricBoList(applicationName, metricName, fieldName, tags, Range.newRange(from, to));
    }

    @RequestMapping(value = "/systemMetricChart")
    @ResponseBody
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("tags") List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "interval", required = false)Integer interval){
        final int minSamplingInterval = 10;
        final long intervalMs = interval != null && interval > minSamplingInterval? interval * 1000L : minSamplingInterval * 1000L;
        TimeWindowSampler sampler = new TimeWindowSampler() {
            @Override
            public long getWindowSize(Range range) {
                return intervalMs;
            }
        };
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);
        return systemMetricService.getSystemMetricChart(applicationName, metricName, fieldName, tags, timeWindow);
    }
}
