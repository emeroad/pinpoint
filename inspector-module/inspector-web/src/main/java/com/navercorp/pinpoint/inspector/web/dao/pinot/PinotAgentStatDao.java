/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.inspector.web.dao.pinot;

import com.navercorp.pinpoint.common.model.SortKeyUtils;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.web.dao.model.InspectorQueryParameter;
import com.navercorp.pinpoint.inspector.web.definition.metric.field.Field;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author minwoo.jung
 */
@Repository("pinotAgentStatDao")
public class PinotAgentStatDao implements AgentStatDao {

    private static final String NAMESPACE = PinotAgentStatDao.class.getName() + ".";
    private final PinotAsyncTemplate asyncTemplate;
    private final SqlSessionTemplate syncTemplate;
    private final TableNameManager tableNameManager;

    public PinotAgentStatDao(@Qualifier("inspectorPinotAsyncTemplate") PinotAsyncTemplate asyncTemplate, @Qualifier("inspectorPinotTemplate") SqlSessionTemplate syncTemplate, InspectorWebProperties inspectorWebProperties) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.syncTemplate = Objects.requireNonNull(syncTemplate, "syncTemplate");
        Objects.requireNonNull(inspectorWebProperties, "inspectorWebProperties");
        this.tableNameManager = new TableNameManager(inspectorWebProperties.getAgentStatTablePrefix(), inspectorWebProperties.getAgentStatTablePaddingLength(), inspectorWebProperties.getAgentStatTableCount());
    }

    @Override
    public CompletableFuture<List<DataPoint<Double>>> selectAgentStatAvg(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorAvgData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<DataPoint<Double>>> selectAgentStatMax(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorMaxData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<DataPoint<Double>>> selectAgentStatSum(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorSumData", inspectorQueryParameter);
    }

    @Override
    public CompletableFuture<List<DataPoint<Double>>> selectAgentStat(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName(), field.getTags());
        return asyncTemplate.selectList(NAMESPACE + "selectInspectorData", inspectorQueryParameter);
    }

    @Override
    public List<Tag> getTagInfo(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field) {
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName());
        return syncTemplate.selectList(NAMESPACE + "selectTagInfo", inspectorQueryParameter);
    }

    @Override
    public TagInformation getTagInfoContainedSpecificTag(InspectorDataSearchKey inspectorDataSearchKey, String metricName, Field field, Tag tag) {
        List<Tag> tagList = List.of(tag);
        InspectorQueryParameter inspectorQueryParameter = new InspectorQueryParameter(inspectorDataSearchKey, getTableName(inspectorDataSearchKey), generateKeyForAgentStat(inspectorDataSearchKey, metricName), metricName, field.getFieldName(), tagList);

        return syncTemplate.selectOne(NAMESPACE + "selectTagInfoContainedSpecificTag", inspectorQueryParameter);
    }

    private String generateKeyForAgentStat(InspectorDataSearchKey inspectorDataSearchKey, String metricName) {
        return SortKeyUtils.generateKeyForAgentStat(inspectorDataSearchKey.getApplicationName(), inspectorDataSearchKey.getAgentId(), metricName);
    }

    private String getTableName(InspectorDataSearchKey inspectorDataSearchKey) {
        return tableNameManager.getTableName(inspectorDataSearchKey.getApplicationName());
    }
}
