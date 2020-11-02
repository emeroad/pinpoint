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

package com.navercorp.pinpoint.web.metric.dao.druid;

import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import com.navercorp.pinpoint.web.metric.dao.SystemMetricDao;
import com.navercorp.pinpoint.web.metric.mapper.druid.DruidSystemMetricMapper;
import com.navercorp.pinpoint.web.metric.util.druid.DruidQueryStatementWriter;
import com.navercorp.pinpoint.web.metric.vo.SampledSystemMetric;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Hyunjoon Cho
 */
//@Repository
public class DruidSystemMetricDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String druidUrl;
    private final Properties properties;
    private Connection connection;
    private final DruidQueryStatementWriter queryStatementWriter;
    private final DruidSystemMetricMapper systemMetricMapper;

    public DruidSystemMetricDao(DruidQueryStatementWriter queryStatementWriter,
                                DruidSystemMetricMapper systemMetricMapper) {
        this.queryStatementWriter = Objects.requireNonNull(queryStatementWriter, "queryStatementWriter");
        this.systemMetricMapper = Objects.requireNonNull(systemMetricMapper, "systemMetricMapper");

        this.druidUrl = "jdbc:avatica:remote:url=http://IP:PORT/druid/v2/sql/avatica/";
        this.properties = new Properties();
        properties.put("useApproximateTopN", "false");
        try {
            this.connection = DriverManager.getConnection(druidUrl, properties);
        } catch (SQLException e) {
            logger.warn("Druid Connection Failed {}", e.getMessage());
        }
    }

    @PreDestroy
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.warn("Closing Druid Connection Failed {}", e.getMessage());
        }
    }
}
