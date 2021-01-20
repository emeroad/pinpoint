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

package com.navercorp.pinpoint.web.metric.util.druid;

import com.navercorp.pinpoint.common.server.metric.model.Tag;
import com.navercorp.pinpoint.web.metric.util.QueryStatementWriter;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Hyunjoon Cho
 */
//@Component
public class DruidQueryStatementWriter extends QueryStatementWriter {
    private final SimpleDateFormat format;

    public DruidQueryStatementWriter() {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String queryForMetricNameList(String applicationName, boolean isLong) {
        buildBasicQuery(true, "metricName", "\"system-metric\"");
        addWhereStatement("applicationName", applicationName);
        return build();
    }

    @Override
    public String queryForFieldNameList(String applicationName, String metricName, boolean isLong) {
        buildBasicQuery(true, "fieldName", "\"system-metric\"");
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);

        return build();
    }


    @Override
    public String queryForTagBoList(String applicationName, String metricName, String fieldName, boolean isLong, long timestamp) {
        buildBasicQuery(true, "tags", "\"system-metric\"");
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        addAndStatement("fieldName", fieldName);

        return build();
    }

    @Override
    public String queryForSystemMetricBoList(String applicationName, String metricName, String fieldName, List<Tag> tags, boolean isLong, Range range) {
        buildBasicQuery(false, "*", "\"system-metric\"");
        addWhereStatement("applicationName", applicationName);
        addAndStatement("metricName", metricName);
        addAndStatement("fieldName", fieldName);

        for (Tag tag : tags) {
            addContainsStringStatement("tags", tag.toString());
        }

        addRangeStatement(range);

        return build();
    }

    @Override
    public String queryForSampledSystemMetric(String applicationName, String metricName, String fieldName, List<Tag> tags, boolean isLong, TimeWindow timeWindow) {
        return null;
    }

    private void addContainsStringStatement(String key, String value) {
        queryBuilder.append(" AND ").append("CONTAINS_STRING(").append(key).append(",'").append(value).append("')");
    }

    private void addRangeStatement(Range range) {
        queryBuilder.append(" AND ").append("__time").append(" >= '").append(format.format(new Date(range.getFrom()))).append('\'')
                .append(" AND ").append("__time").append(" <= '").append(format.format(new Date(range.getTo()))).append('\'');
    }
}
