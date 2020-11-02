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

package com.navercorp.pinpoint.collector.metric.util.druid;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Properties;

/**
 * @author Hyunjoon Cho
 */
//@Component
public class DruidKafkaProducer {
    private Properties configs;
    private final KafkaProducer<String, String> kafkaProducer;
    private static final String TOPIC = "system-metric-topic";
    private static final String BOOTSTRAP_SERVERS = "IP:PORT";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    may turn to provider or factory if Kafka is required more widely

    public DruidKafkaProducer() {
        // read from config or get as parameter
        // for now, get as argument
        configs = new Properties();
        configs.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        configs.put("acks", "all");
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<>(configs);
    }

    public void pushData(List<String> systemMetricStringList) {
        logger.info("before time {}", System.currentTimeMillis());
        for (String systemMetric : systemMetricStringList) {
//            logger.info(systemMetric);
            kafkaProducer.send(new ProducerRecord<>(TOPIC, systemMetric));
        }
        kafkaProducer.flush();
        logger.info("after time {}", System.currentTimeMillis());
    }

    @PreDestroy
    public void closeProducer() {
        if (kafkaProducer != null){
            kafkaProducer.close();
            logger.info("Kafka Producer Closed");
        }
    }
}
