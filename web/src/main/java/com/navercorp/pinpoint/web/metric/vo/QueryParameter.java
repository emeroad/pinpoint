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

package com.navercorp.pinpoint.web.metric.vo;

import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hyunjoon Cho
 */
public class QueryParameter {
    private static final int TAG_SET_COUNT = 10;

    String tableName, applicationName, metricName, fieldName;
    List<TagBo> tagBoList;
    Range range;
    Long intervalMs;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setTagBoList(List<TagBo> tagBoList) {
        this.tagBoList = tagBoList;
    }

    public List<TagBo> getTagBoList() {
        return tagBoList;
    }

    public void setRange(Range range) {
        this.range = range;

    }

    public Range getRange() {
        return range;
    }

    public void setIntervalMs(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    private long estimateLimit(Range range) {
        return (range.getRange() / 10000 + 1) * TAG_SET_COUNT;
    }

    public Map toMap() {
        Map parameters = new HashMap();
        parameters.put("tableName", tableName);
        parameters.put("applicationName", applicationName);
        parameters.put("metricName", metricName);
        parameters.put("fieldName", fieldName);
        parameters.put("tags", tagBoList);
        parameters.put("range", range);
        parameters.put("limit", estimateLimit(range));
        if (intervalMs != null) {
            parameters.put("interval", intervalMs);
        }

        return parameters;
    }
}
