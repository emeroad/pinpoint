/*
 * Copyright 2020 NAVER Corp.
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

package test.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

/**
 *
 */
public class Kafka3UnitServer implements SharedTestLifeCycle {
    public static final OffsetStore OFFSET_STORE = new OffsetStore();
    private static final Logger logger = LogManager.getLogger(Kafka3UnitServer.class);


    private KafkaContainer container;
    private TestConsumer TEST_CONSUMER;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

        container.start();
        int port = container.getFirstMappedPort();

        String brokerUrl = "localhost:" + port;
        TEST_CONSUMER = new TestConsumer(OFFSET_STORE, brokerUrl);
        TEST_CONSUMER.start();

        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(port));
        properties.setProperty("OFFSET", String.valueOf(OFFSET_STORE.getOffset()));
        System.getProperties().putAll(properties);
        return properties;
    }

    @Override
    public void afterAll() {
        if (TEST_CONSUMER != null) {
            try {
                TEST_CONSUMER.shutdown();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        if (container != null) {
            container.stop();
        }
    }
}
