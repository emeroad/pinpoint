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

package com.navercorp.pinpoint.web.metric.dao.pinot;

import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.web.metric.dao.SystemMetricDao;
import com.navercorp.pinpoint.web.metric.mapper.pinot.PinotSystemMetricLongMapper;
import com.navercorp.pinpoint.web.metric.util.SystemMetricTemplate;
import com.navercorp.pinpoint.web.metric.vo.QueryParameter;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import com.navercorp.pinpoint.web.util.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricLongDao implements SystemMetricDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String LONG_TABLE = "systemMetricLong";

    private final SystemMetricTemplate systemMetricTemplate;
    private final PinotSystemMetricLongMapper pinotSystemMetricLongMapper;

    public PinotSystemMetricLongDao(SystemMetricTemplate systemMetricTemplate,
                                    PinotSystemMetricLongMapper pinotSystemMetricLongMapper) {
        this.systemMetricTemplate = Objects.requireNonNull(systemMetricTemplate, "systemMetricTemplate");
        this.pinotSystemMetricLongMapper = Objects.requireNonNull(pinotSystemMetricLongMapper, "pinotSystemMetricLongMapper");
    }

    @Override
    public List<SystemMetricBo> getSystemMetricBo(QueryParameter queryParameter) {
        queryParameter.setTableName(LONG_TABLE);
        return systemMetricTemplate.selectSystemMetricBoList(queryParameter, pinotSystemMetricLongMapper);
    }

    @Override
    public List<SampledSystemMetric<Long>> getSampledSystemMetric(QueryParameter queryParameter) {
        queryParameter.setTableName(LONG_TABLE);
        return systemMetricTemplate.selectSampledSystemMetricList(queryParameter, pinotSystemMetricLongMapper);
    }
}
