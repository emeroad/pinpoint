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

package com.navercorp.pinpoint.web.metric.util;

import com.navercorp.pinpoint.common.server.metric.model.SystemMetricBo;
import com.navercorp.pinpoint.web.metric.mapper.SystemMetricMapper;
import com.navercorp.pinpoint.web.metric.vo.QueryParameter;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Component
public class SystemMetricTemplate {
    private static final String NAMESPACE = SystemMetricTemplate.class.getPackage().getName() + "." + SystemMetricTemplate.class.getSimpleName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;

    public SystemMetricTemplate(SqlSessionTemplate sqlPinotSessionTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
    }

    public List<SystemMetricBo> selectSystemMetricBoList(QueryParameter queryParameter, SystemMetricMapper systemMetricMapper) {
        Map parameters = queryParameter.toMap();
        List<Map<String, String>> resultMap = sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSystemMetric", parameters);
        return systemMetricMapper.mapSystemMetricBo(resultMap, queryParameter.getMetricName(), queryParameter.getFieldName());
    }

    public <T extends Number> List<SampledSystemMetric<T>> selectSampledSystemMetricList(QueryParameter queryParameter, SystemMetricMapper<T> systemMetricMapper) {
        Map parameters = queryParameter.toMap();
        List<Map<String, String>> resultMap = sqlPinotSessionTemplate.selectList(NAMESPACE + "selectSystemMetric", parameters);
        return systemMetricMapper.mapSampledSystemMetric(resultMap);
    }
}
