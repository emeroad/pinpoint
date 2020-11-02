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

package com.navercorp.pinpoint.common.server.metric.bo;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricBo<T extends Number> {
    private String metricName;
    private String fieldName;
    private T fieldValue;
    private List<TagBo> tagBos;
    private long timestamp;

    public SystemMetricBo(String metricName, String fieldName, T fieldValue, List<TagBo> tagBos, long timestamp) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.fieldValue = Objects.requireNonNull(fieldValue, "fieldValue");
        this.tagBos = tagBos;
        this.timestamp = timestamp;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public T getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(T fieldValue) {
        this.fieldValue = fieldValue;
    }

    public List<TagBo> getTagBos() {
        return tagBos;
    }

    public void setTagBos(List<TagBo> tagBos) {
        this.tagBos = tagBos;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemMetric{");
        sb.append("metric=").append(metricName);
        sb.append(", field=").append(fieldName);
        sb.append(", value=").append(fieldValue);
        sb.append(", tags=").append(tagBos);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();

    }
}
