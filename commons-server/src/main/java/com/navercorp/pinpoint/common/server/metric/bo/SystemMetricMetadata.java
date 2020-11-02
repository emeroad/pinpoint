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

package com.navercorp.pinpoint.common.server.metric.bo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricMetadata {
    private final String METADATA_PATH = "/Users/user/pinpoint/commons-server/SystemMetricMetadata.txt";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, MetricType> fieldTypeMap;

    public enum MetricType {
        LongCounter,
        DoubleCounter
    }

    private static class Singleton {
        private static final SystemMetricMetadata systemMetricMetadata = new SystemMetricMetadata();
    }

    private SystemMetricMetadata() {
        fieldTypeMap = loadOrCreate();
    }

    private Map<String, MetricType> loadOrCreate() {
        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(new File(METADATA_PATH)));
            Map<String, MetricType> map = (Map<String, MetricType>) ois.readObject();
            logger.info("Loaded Metadata");
            return map;
        } catch (Exception e) {
            logger.info("Failed Loading Metadata");
            return new HashMap<>();
        }
    }

    public static SystemMetricMetadata getMetadata() {
        return Singleton.systemMetricMetadata;
    }

    public void put(String metricName, String fieldName, MetricType type) {
        fieldTypeMap.put(metricName.concat(fieldName), type);
    }

    public MetricType get(String metricName, String fieldName) {
        return fieldTypeMap.get(metricName.concat(fieldName));
    }

    public void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(new File(METADATA_PATH)));
            oos.writeObject(fieldTypeMap);
            oos.close();
//            logger.info("Saved Metadata");
        } catch (IOException e) {
            logger.warn("Failed to Save Metadata");
        }
    }
}
