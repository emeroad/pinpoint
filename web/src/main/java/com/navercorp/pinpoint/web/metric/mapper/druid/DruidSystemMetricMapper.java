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

package com.navercorp.pinpoint.web.metric.mapper.druid;

import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.TagBo;
import com.navercorp.pinpoint.web.metric.util.SystemMetricUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Hyunjoon Cho
 */
//@Component
public class DruidSystemMetricMapper {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SimpleDateFormat format;
    public DruidSystemMetricMapper() {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public List<String> processStringList(ResultSet resultSet) throws SQLException {
        List<String> stringList = new ArrayList<>();
        while (resultSet.next()) {
            stringList.add(resultSet.getString(1));
        }
        return stringList;
    }

    public List<TagBo> processTagBoList(ResultSet resultSet) throws SQLException {
        List<TagBo> tagBoList = new ArrayList<>();
        while (resultSet.next()) {
            tagBoList.add(parseTagBo(resultSet.getString(1)));
        }
        return tagBoList;
    }

    public List<SystemMetricBo> processSystemMetricBoList(ResultSet resultSet) throws SQLException {
        List<SystemMetricBo> systemMetricBoList = new ArrayList<>();
        while (resultSet.next()) {
            if (resultSet.getDouble("fieldDoubleValue") == -1) {
                systemMetricBoList.add(new SystemMetricBo<>(resultSet.getString("metricName"),
                        resultSet.getString("fieldName"),
                        resultSet.getLong("fieldLongValue"),
                        parseTagBoList(resultSet.getString("tags")),
                        parseTimestamp(resultSet.getString("__time"))));
            } else {
                systemMetricBoList.add(new SystemMetricBo<>(resultSet.getString("metricName"),
                        resultSet.getString("fieldName"),
                        resultSet.getDouble("fieldDoubleValue"),
                        parseTagBoList(resultSet.getString("tags")),
                        parseTimestamp(resultSet.getString("__time"))));
            }

        }
        return systemMetricBoList;
    }

    public List<TagBo> parseTagBoList(String tags) {
        List<TagBo> tagBoList = new ArrayList<>();

//        logger.info("tags {}", tags);
        if (tags.startsWith("[")) {
            String[] tagStrings = SystemMetricUtils.parseMultiValueFieldList(tags);
            for (String tagString : tagStrings) {
                tagBoList.add(parseTagBo(tagString));
            }
        } else {
            tagBoList.add(parseTagBo(tags));
        }

        return tagBoList;
    }

    public TagBo parseTagBo(String tagString) {
        String[] tag = tagString.split(":");
        return new TagBo(tag[0], tag[1]);
    }

    public long parseTimestamp(String time) {
        try {
            return format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
