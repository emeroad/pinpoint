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

import com.navercorp.pinpoint.common.server.metric.model.Tag;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hyunjoon Cho
 */
public class QueryParameter {
    private static final int TAG_SET_COUNT = 10;

    private String applicationName;
    private String metricName;
    private String fieldName;
    private List<Tag> tagList;
    private Range range;
    private long limit;

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

    public void setTagList(List<Tag> tagList) {
        this.tagList = tagList;
    }

    public List<Tag> getTagList() {
        return tagList;
    }

    public void setRange(Range range) {
        this.range = range;
        this.limit = estimateLimit(range);
    }

    public Range getRange() {
        return range;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    private long estimateLimit(Range range) {
        return (range.getRange() / 10000 + 1) * TAG_SET_COUNT;
    }
}
