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

package com.navercorp.pinpoint.web.metric.util.pinot;

import com.navercorp.pinpoint.common.server.metric.model.Tag;
import com.navercorp.pinpoint.web.metric.util.QueryStatementWriter;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hyunjoon Cho
 */
@Component
public class PinotQueryStatementWriter extends QueryStatementWriter {

    private final static String LONG_DB = "systemMetricLong";
    private final static String DOUBLE_DB = "systemMetricDouble";

    @Override
    public String queryForMetricNameList(String applicationName, boolean isLong) {
        String db = isLong? LONG_DB : DOUBLE_DB;
        buildBasicQuery(true, "metricName", db);
        addWhereStatement("applicationName", applicationName);
        return build();
    }

    @Override
    public String queryForFieldNameList(String applicationName, String metricName, boolean isLong) {
        String db = isLong? LONG_DB : DOUBLE_DB;
        buildBasicQuery(true, "fieldName", db);
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        setLimit(100);
        return build();
    }

    public String queryTimestampForField(String applicationName, String metricName, String fieldName, boolean isLong) {
        String db = isLong? LONG_DB : DOUBLE_DB;
        buildBasicQuery(true, "timestampInEpoch", db);
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        addAndStatement("fieldName", fieldName);
        setLimit(1);

        return build();
    }

    @Override
    public String queryForTagBoList(String applicationName, String metricName, String fieldName, boolean isLong, long timestamp) {
        String db = isLong? LONG_DB : DOUBLE_DB;
        buildBasicQuery(false, "tagName, tagValue", db);
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        addAndStatement("fieldName", fieldName);
        addAndStatement("timestampInEpoch", timestamp);

        return build();
    }

    @Override
    public String queryForSystemMetricBoList(String applicationName, String metricName, String fieldName, List<Tag> tags, boolean isLong, Range range) {
        basicStatementForSystemMetric(applicationName, metricName, fieldName, tags, isLong, range);

        long expectedLimit = ((range.getTo() - range.getFrom())/10000 - 1) * 10;
        // by default, telegraf collect every 10sec = 10000ms
        // make it configurable
        setLimit(expectedLimit);

        return build();
    }

    @Override
    public String queryForSampledSystemMetric(String applicationName, String metricName, String fieldName, List<Tag> tags, boolean isLong, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowRange();
        basicStatementForSystemMetric(applicationName, metricName, fieldName, tags, isLong, range);

        addSamplingCondition(timeWindow.getWindowSlotSize());

        long expectedLimit = ((range.getTo() - range.getFrom())/10000 - 1) * 10;
        setLimit(expectedLimit);

        return build();
    }

    private void basicStatementForSystemMetric(String applicationName, String metricName, String fieldName, List<Tag> tags, boolean isLong, Range range) {
        String db = isLong? LONG_DB : DOUBLE_DB;
        buildBasicQuery(false, "*", db);
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        addAndStatement("fieldName", fieldName);

        for (Tag tag : tags) {
            addAndStatement("tagName", tag.getName());
            addAndStatement("tagValue", tag.getValue());
        }
        addRangeStatement(range);
    }

    private void addRangeStatement(Range range) {
        queryBuilder.append(" AND ").append("timestampInEpoch").append(" >= ").append(range.getFrom())
                .append(" AND ").append("timestampInEpoch").append(" <= ").append(range.getTo());
    }

    private void addSamplingCondition(long intervalMs) {
        if (intervalMs != 10000L) {
            queryBuilder.append(" AND ").append("timestampInEpoch").append(" % ").append(intervalMs).append(" = 0");
        }
    }
}
