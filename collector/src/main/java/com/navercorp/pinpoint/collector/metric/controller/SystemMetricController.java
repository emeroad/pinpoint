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

package com.navercorp.pinpoint.collector.metric.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navercorp.pinpoint.collector.metric.service.SystemMetricService;
import com.navercorp.pinpoint.collector.metric.vo.SystemMetricJsonDeserializer;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricBo;
import com.navercorp.pinpoint.common.server.metric.bo.SystemMetricMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Controller
public class SystemMetricController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SystemMetricService systemMetricService;
    private final ObjectMapper objectMapper;

    public SystemMetricController(SystemMetricService systemMetricService){
        this.systemMetricService = Objects.requireNonNull(systemMetricService, "systemMetricService");
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SystemMetricBo.class, new SystemMetricJsonDeserializer());
        objectMapper.registerModule(module);

    }

    @RequestMapping(value = "/telegraf", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void saveSystemMetric(
            @RequestHeader(value = "Application-Name")String applicationName,
            @RequestBody String body) throws JsonProcessingException {
//        logger.info("controller time {}", System.currentTimeMillis());

        List<SystemMetricBo> systemMetricBos;
        try {
            JsonNode jsonNode = objectMapper.readTree(body).get("metrics");
            systemMetricBos = Arrays.asList(objectMapper.readValue(jsonNode.toString(), SystemMetricBo[].class));
            SystemMetricMetadata.getMetadata().save();
        } catch (IOException e) {
            systemMetricBos = null;
            logger.warn("System Metric Deserialization Failed: {}", e.getMessage());
        }

        systemMetricService.insert(applicationName, systemMetricBos);
    }
}