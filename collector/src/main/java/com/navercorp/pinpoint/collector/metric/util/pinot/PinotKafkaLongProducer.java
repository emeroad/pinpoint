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

package com.navercorp.pinpoint.collector.metric.util.pinot;

import com.navercorp.pinpoint.collector.metric.util.SystemMetricKafkaProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Properties;

/**
 * @author Hyunjoon Cho
 */
@Component
public class PinotKafkaLongProducer implements SystemMetricKafkaProducer {
    private Properties configs;
    private final KafkaProducer<String, String> kafkaProducer;
    private static final String BOOTSTRAP_SERVERS = "10.113.84.89:19092";
    private static final String TOPIC = "system-metric-long";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PinotKafkaLongProducer() {
        configs = new Properties();
        configs.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        configs.put("acks", "all");
        configs.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configs.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<>(configs);
    }

    @Override
    public void pushData(List<String> systemMetricStringList) {
        for (String systemMetric : systemMetricStringList) {
            kafkaProducer.send(new ProducerRecord<>(TOPIC, systemMetric));
        }
        kafkaProducer.flush();
    }

    @PreDestroy
    public void closeProducer() {
        if (kafkaProducer != null){
            kafkaProducer.close();
            logger.info("Kafka Producer Closed");
        }
    }
}
