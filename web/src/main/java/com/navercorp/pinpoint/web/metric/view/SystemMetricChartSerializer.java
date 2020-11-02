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

package com.navercorp.pinpoint.web.metric.view;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.navercorp.pinpoint.web.metric.vo.chart.SystemMetricChart;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricChartSerializer extends JsonSerializer<SystemMetricChart> {
    @Override
    public void serialize(SystemMetricChart systemMetricChart, JsonGenerator jgen, SerializerProvider serializers) throws IOException {
        SystemMetricChart.SystemMetricChartGroup systemMetricChartGroup = systemMetricChart.getSystemMetricChartGroup();
        jgen.writeStartObject();
        jgen.writeObjectField("title", systemMetricChartGroup.getChartName());

        TimeWindow timeWindow = systemMetricChartGroup.getTimeWindow();
        writeTimestamp(jgen, timeWindow);

        List<String> tags = systemMetricChartGroup.getTagsList();
        List<Chart<? extends Point>> charts = systemMetricChartGroup.getCharts();
        writeCharts(jgen, tags, charts);
        jgen.writeEndObject();
    }

    private void writeTimestamp(JsonGenerator jgen, TimeWindow timeWindow) throws IOException {
        List<Long> timestamps = new ArrayList<>((int) timeWindow.getWindowRangeCount());
        for (Long timestamp : timeWindow) {
            timestamps.add(timestamp);
        }
        jgen.writeObjectField("x", timestamps);
    }

    private void writeCharts(JsonGenerator jgen, List<String> tags, List<Chart<? extends Point>> charts) throws IOException {
        jgen.writeFieldName("y");
        jgen.writeStartObject();
        int l = tags.size();
        for (int i = 0; i < l; i++) {
            Chart<? extends Point> chart = charts.get(i);
            List<? extends Point> points = chart.getPoints();
            jgen.writeObjectField(tags.get(i), points);
        }
        jgen.writeEndObject();
    }
}
