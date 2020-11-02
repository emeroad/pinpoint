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

package com.navercorp.pinpoint.web.metric;

import com.navercorp.pinpoint.web.metric.dao.pinot.PinotSystemMetricLongDao;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.pinot.client.PinotDriver;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Hyunjoon Cho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/pinot/applicationContext-pinot-test.xml"})
public class MybatisTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Test
    public void testDao() {
//        PinotSystemMetricLongDao dao = new PinotSystemMetricLongDao(sqlSessionTemplate);
//        System.out.println(dao.getMetricNameList("hyunjoon.cho"));
    }

    @Test
    public void testConnection() throws Exception {
        Connection connection = dataSource.getConnection();
        System.out.println(connection.toString());
        connection.close();
    }

    @Test
    public void testFactory() {
        SqlSession session = sqlSessionFactory.openSession();
        System.out.println(session);
    }

    @Test
    public void testTemplate() {
        System.out.println(sqlSessionTemplate);
    }

    @Test
    public void testDirectConnection() throws SQLException {
        String dbURL = "jdbc:pinot://10.113.84.89:8099?controller=10.113.84.89:9000";
        DriverManager.registerDriver(new PinotDriver());
        Connection connection = DriverManager.getConnection(dbURL);
        PreparedStatement statement = connection.prepareStatement("SELECT metricName AS name FROM baseballStats WHERE age = ?");
        statement.setInt(1, 20);

        System.out.println(connection.toString());
        connection.close();
    }
}
