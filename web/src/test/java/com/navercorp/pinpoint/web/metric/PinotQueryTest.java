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

package com.navercorp.pinpoint.web.metric;

import org.apache.pinot.client.Connection;
import org.apache.pinot.client.ConnectionFactory;
import org.apache.pinot.client.Request;
import org.apache.pinot.client.ResultSet;
import org.apache.pinot.client.ResultSetGroup;
import org.junit.Test;

/**
 * @author Hyunjoon Cho
 */
public class PinotQueryTest {
    @Test
    public void simplePinotQueryTest() {
        String zkURL = "IP:PORT";
        String pinotClusterName = "PinotCluster";
        Connection pinotConnection = ConnectionFactory.fromZookeeper(zkURL + "/" + pinotClusterName);

        String query = "SELECT COUNT(*) FROM systemMetric WHERE metricName='cpu'";

        Request pinotClientRequest = new Request("sql", query);
        ResultSetGroup pinotResultSetGroup = pinotConnection.execute(pinotClientRequest);
        ResultSet resultTableResultSet = pinotResultSetGroup.getResultSet(0);

//        int numRows = resultTableResultSet.getRowCount();
//        int numColumns = resultTableResultSet.getColumnCount();
        String columnValue = resultTableResultSet.getString(0, 0);
        String columnName = resultTableResultSet.getColumnName(0);

        System.out.println("ColumnName: " + columnName + ", ColumnValue: " + columnValue);
    }
}
