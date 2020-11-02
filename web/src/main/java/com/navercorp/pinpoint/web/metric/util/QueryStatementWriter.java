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

package com.navercorp.pinpoint.web.metric.util;

import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public abstract class QueryStatementWriter {
    protected StringBuilder queryBuilder;
    public abstract String queryForMetricNameList(String applicationName, boolean isLong);
    public abstract String queryForFieldNameList(String applicationName, String metricName, boolean isLong);
    public abstract String queryForTagBoList(String applicationName, String metricName, String fieldName, boolean isLong, long timestamp);
    public abstract String queryForSystemMetricBoList(String applicationName, String metricName, String fieldName, List<TagBo> tagBos, boolean isLong, Range range);
    public abstract String queryForSampledSystemMetric(String applicationName, String metricName, String fieldName, List<TagBo> tagBos, boolean isLong, TimeWindow timeWindow);

    protected void buildBasicQuery(boolean distinct, String target, String db) {
        queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ");
        if (distinct) {
            queryBuilder.append("DISTINCT ");
        }
        queryBuilder.append(target);
        queryBuilder.append(" FROM ").append(db);
        // systemMetric for Pinot
        // \"system-metric\" for Druid
    }

    protected void addWhereStatement(String key, String value) {
        queryBuilder.append(" WHERE ").append(key).append("='").append(value).append("'");
    }

    protected void addAndStatement(String key, String value) {
        queryBuilder.append(" AND ").append(key).append("='").append(value).append("'");
    }

    protected void addAndStatement(String key, long value) {
        queryBuilder.append(" AND ").append(key).append("=").append(value);
    }

//    private StringBuilder addOrStatement(StringBuilder query, String key, String value) {
//        return query.append(" OR ").append(key).append("='").append(value).append("'");
//    }

    protected void setLimit( long limit) {
        queryBuilder.append(" LIMIT ").append(limit);
    }

    public String build() {
        return queryBuilder.toString();
    }
}
