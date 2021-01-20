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

package com.navercorp.pinpoint.common.server.metric.model;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */

public class SystemMetricBo<T extends Number> {
    private final String metricName;
    private final String fieldName;
    private final T fieldValue;
    private final List<Tag> tags;
    private final long timestamp;

    public SystemMetricBo(String metricName, String fieldName, T fieldValue, List<Tag> tags, long timestamp) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName");
        this.fieldValue = Objects.requireNonNull(fieldValue, "fieldValue");
        this.tags = tags;
        this.timestamp = timestamp;
    }

    public String getMetricName() {
        return metricName;
    }


    public String getFieldName() {
        return fieldName;
    }

    public T getFieldValue() {
        return fieldValue;
    }

    public List<Tag> getTagBos() {
        return tags;
    }


    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemMetric{");
        sb.append("metric=").append(metricName);
        sb.append(", field=").append(fieldName);
        sb.append(", value=").append(fieldValue);
        sb.append(", tags=").append(tags);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();

    }
}
