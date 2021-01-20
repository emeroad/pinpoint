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
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricTypeHandler implements TypeHandler<SystemMetricBo> {

    @Override
    public void setParameter(PreparedStatement ps, int i, SystemMetricBo parameter, JdbcType jdbcType) throws SQLException {

    }

    @Override
    public SystemMetricBo getResult(ResultSet rs, String columnName) throws SQLException {
        return null;
    }

    @Override
    public SystemMetricBo getResult(ResultSet rs, int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SystemMetricBo getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return null;
    }
}
