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

package com.navercorp.pinpoint.metric.web.controller;

import com.navercorp.pinpoint.metric.common.model.SystemMetricBo;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.metric.web.service.SystemMetricService;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TagParser;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.vo.chart.SystemMetricChart;
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
    private final TagParser tagParser = new TagParser();

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
        List<Tag> tagList = tagParser.parseTags(tags);
        return systemMetricService.getSystemMetricBoList(applicationName, metricName, fieldName, tagList, Range.newRange(from, to));
    }

    @RequestMapping(value = "/systemMetricChart")
    @ResponseBody
    public SystemMetricChart getSystemMetricChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("metricName") String metricName,
            @RequestParam("fieldName") String fieldName,
            @RequestParam("tags") List<String> tags,
            @RequestParam("from") long from,
            @RequestParam("to") long to){
        final long intervalMs = 10000L;
        TimeWindowSampler sampler = new TimeWindowSampler() {
            @Override
            public long getWindowSize(Range range) {
                return intervalMs;
            }
        };
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), sampler);

        List<Tag> tagList = tagParser.parseTags(tags);

        return systemMetricService.getSystemMetricChart(applicationName, metricName, fieldName, tagList, timeWindow);
    }
}
