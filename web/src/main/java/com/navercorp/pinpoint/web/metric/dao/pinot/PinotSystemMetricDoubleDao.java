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

import com.navercorp.pinpoint.common.server.metric.model.SystemMetricBo;
import com.navercorp.pinpoint.web.metric.dao.SystemMetricDao;
import com.navercorp.pinpoint.web.metric.mapper.pinot.PinotSystemMetricDoubleMapper;
import com.navercorp.pinpoint.web.metric.util.SystemMetricTemplate;
import com.navercorp.pinpoint.web.metric.vo.QueryParameter;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String DOUBLE_TABLE = "systemMetricDouble";

    private final SystemMetricTemplate systemMetricTemplate;
    private final PinotSystemMetricDoubleMapper pinotSystemMetricDoubleMapper;

    public PinotSystemMetricDoubleDao(SystemMetricTemplate systemMetricTemplate,
                                      PinotSystemMetricDoubleMapper pinotSystemMetricDoubleMapper) {
        this.systemMetricTemplate = Objects.requireNonNull(systemMetricTemplate, "systemMetricTemplate");
        this.pinotSystemMetricDoubleMapper = Objects.requireNonNull(pinotSystemMetricDoubleMapper, "pinotSystemMetricDoubleMapper");
    }

    @Override
    public List<SystemMetricBo> getSystemMetricBo(QueryParameter queryParameter) {
        queryParameter.setTableName(DOUBLE_TABLE);
        return systemMetricTemplate.selectSystemMetricBoList(queryParameter, pinotSystemMetricDoubleMapper);
    }

    @Override
    public List<SampledSystemMetric<Double>> getSampledSystemMetric(QueryParameter queryParameter) {
        queryParameter.setTableName(DOUBLE_TABLE);
        return systemMetricTemplate.selectSampledSystemMetricList(queryParameter, pinotSystemMetricDoubleMapper);
    }
}
