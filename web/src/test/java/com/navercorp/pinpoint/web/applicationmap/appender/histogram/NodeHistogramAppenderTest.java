/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class NodeHistogramAppenderTest {

    private final Logger logger = LogManager.getLogger(getClass());

    @AutoClose("shutdown")
    private static final Executor executor = Executors.newFixedThreadPool(4);

    private WasNodeHistogramDataSource wasNodeHistogramDataSource;

    private NodeHistogramAppender nodeHistogramAppender;

    private long buildTimeoutMillis = 1000;

    @BeforeEach
    public void setUp() {
        wasNodeHistogramDataSource = mock(WasNodeHistogramDataSource.class);
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
        NodeHistogramAppenderFactory factory = new NodeHistogramAppenderFactory(executor);
        nodeHistogramAppender = factory.create(nodeHistogramFactory);
    }

    @Test
    public void emptyNodeList() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        NodeList nodeList = NodeList.of();
        LinkList linkList = LinkList.of();
        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);
        // Then
        assertThat(nodeList.getNodeList()).isEmpty();
        verifyNoInteractions(wasNodeHistogramDataSource);
    }

    /**
     * Checks histograms for was node.
     */
    @Test
    public void wasNode() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        LinkList linkList = LinkList.of();
        Node node = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        NodeList nodeList = NodeList.of(node);

        NodeHistogram nodeHistogram = NodeHistogram.empty(node.getApplication(), range);
        when(wasNodeHistogramDataSource.createNodeHistogram(node.getApplication(), timeWindow)).thenReturn(nodeHistogram);
        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);
        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        Assertions.assertSame(nodeHistogram, actualNode.getNodeHistogram());
    }

    /**
     * Checks histograms for a single terminal node.
     * <pre>
     *     fromNode ---> databaseNode
     * </pre>
     */
    @Test
    public void terminalNode() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // toNode : [testDatabase] test-database
        Node toNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String toNodeAgent = "test-database";
        NodeList nodeList = NodeList.of(toNode);

        Link link = new Link(LinkDirection.IN_LINK, fromNode, toNode, range);
        HistogramSlot fastSlot = toNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = toNode.getServiceType().getHistogramSchema().getNormalSlot();
        HistogramSlot slowSlot = toNode.getServiceType().getHistogramSchema().getSlowSlot();
        // [testApp] test-app -> [testDatabase] test-database
        long fastCallCount = 200L;
        long normalCallCount = 100L;
        long slowCallCount = 75L;
        link.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), fastSlot, fastCallCount));
        link.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), normalSlot, normalCallCount));
        link.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), slowSlot, slowCallCount));
        LinkList linkList = LinkList.of(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);

        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        NodeHistogram nodeHistogram = actualNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        assertEquals(fastCallCount, applicationHistogram.getFastCount());
        assertEquals(normalCallCount, applicationHistogram.getNormalCount());
        assertEquals(slowCallCount, applicationHistogram.getSlowCount());
        assertEquals(fastCallCount + normalCallCount + slowCallCount, applicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        Histogram agentHistogram = agentHistogramMap.get(toNodeAgent);
        assertEquals(fastCallCount, agentHistogram.getFastCount());
        assertEquals(normalCallCount, agentHistogram.getNormalCount());
        assertEquals(slowCallCount, agentHistogram.getSlowCount());
        assertEquals(fastCallCount + normalCallCount + slowCallCount, agentHistogram.getTotalCount());
    }

    /**
     * Checks histograms for a single terminal node with multiple agents(destinations).
     * <pre>
     *     fromNode ---> databaseNode (2 agents)
     * </pre>
     */
    @Test
    public void terminalNode_multiple() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // toNode : [testDatabase] test-database1, test-database2
        Node toNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String toNodeAgent1 = "test-database1";
        String toNodeAgent2 = "test-database2";
        NodeList nodeList = NodeList.of(toNode);

        Link link = new Link(LinkDirection.IN_LINK, fromNode, toNode, range);
        HistogramSlot fastSlot = toNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = toNode.getServiceType().getHistogramSchema().getNormalSlot();
        // [testApp] test-app -> [testDatabase] test-database1
        long callCount1 = 100L;
        link.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent1, toNode.getServiceType(), fastSlot, callCount1));
        // [testApp] test-app -> [testDatabase] test-database2
        long callCount2 = 50L;
        link.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent2, toNode.getServiceType(), normalSlot, callCount2));
        LinkList linkList = LinkList.of(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);

        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        NodeHistogram nodeHistogram = actualNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        assertEquals(callCount1, applicationHistogram.getFastCount());
        assertEquals(callCount2, applicationHistogram.getNormalCount());
        assertEquals(callCount1 + callCount2, applicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        Histogram agent1Histogram = agentHistogramMap.get(toNodeAgent1);
        assertEquals(callCount1, agent1Histogram.getFastCount());
        assertEquals(callCount1, agent1Histogram.getTotalCount());
        Histogram agent2Histogram = agentHistogramMap.get(toNodeAgent2);
        assertEquals(callCount2, agent2Histogram.getNormalCount());
        assertEquals(callCount2, agent2Histogram.getTotalCount());
    }

    /**
     * Checks histograms for multiple terminal nodes called from a single node.
     * <pre>
     *     fromNode ---> databaseNode
     *               |-> cacheNode
     * </pre>
     */
    @Test
    public void terminalNodes() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        NodeList.Builder nodeBuilder = NodeList.newBuilder();
        LinkList.Builder linkBuilder = LinkList.newBuilder();

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // databaseNode : [testDatabase] test-database
        Node databaseNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String databaseNodeAgent = "test-database";
        nodeBuilder.addNode(databaseNode);
        // cacheNode : [testCache] test-cache
        Node cacheNode = createNode("testCache", ServiceTypeFactory.of(8000, "Cache", ServiceTypeProperty.TERMINAL));
        String cacheNodeAgent = "test-cache";
        nodeBuilder.addNode(cacheNode);
        NodeList nodeList = nodeBuilder.build();

        Link databaseLink = new Link(LinkDirection.IN_LINK, fromNode, databaseNode, range);
        HistogramSlot databaseSlowSlot = databaseNode.getServiceType().getHistogramSchema().getSlowSlot();
        long databaseCallSlowCount = 50L;
        databaseLink.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), databaseNodeAgent, databaseNode.getServiceType(), databaseSlowSlot, databaseCallSlowCount));
        linkBuilder.addLink(databaseLink);

        Link cacheLink = new Link(LinkDirection.IN_LINK, fromNode, cacheNode, range);
        HistogramSlot cacheFastSlot = cacheNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot cacheSlowSlot = cacheNode.getServiceType().getHistogramSchema().getSlowSlot();
        long cacheCallFastCount = 199L;
        long cacheCallSlowCount = 99L;
        cacheLink.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), cacheNodeAgent, cacheNode.getServiceType(), cacheFastSlot, cacheCallFastCount));
        cacheLink.addInLink(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), cacheNodeAgent, cacheNode.getServiceType(), cacheSlowSlot, cacheCallSlowCount));
        linkBuilder.addLink(cacheLink);
        LinkList linkList = linkBuilder.build();

        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);

        // Then
        // Database node
        Node actualDatabaseNode = nodeList.findNode(databaseNode.getApplication());
        NodeHistogram databaseNodeHistogram = actualDatabaseNode.getNodeHistogram();
        // verify application-level histogram
        Histogram databaseApplicationHistogram = databaseNodeHistogram.getApplicationHistogram();
        assertEquals(databaseCallSlowCount, databaseApplicationHistogram.getSlowCount());
        assertEquals(databaseCallSlowCount, databaseApplicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> databaseAgentHistogramMap = databaseNodeHistogram.getAgentHistogramMap();
        Histogram databaseAgentHistogram = databaseAgentHistogramMap.get(databaseNodeAgent);
        assertEquals(databaseCallSlowCount, databaseAgentHistogram.getSlowCount());
        assertEquals(databaseCallSlowCount, databaseAgentHistogram.getTotalCount());
        // Cache node
        Node actualCacheNode = nodeList.findNode(cacheNode.getApplication());
        NodeHistogram cacheNodeHistogram = actualCacheNode.getNodeHistogram();
        // verify application-level histogram
        Histogram cacheApplicationHistogram = cacheNodeHistogram.getApplicationHistogram();
        assertEquals(cacheCallFastCount, cacheApplicationHistogram.getFastCount());
        assertEquals(cacheCallSlowCount, cacheApplicationHistogram.getSlowCount());
        assertEquals(cacheCallFastCount + cacheCallSlowCount, cacheApplicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> cacheAgentHistogramMap = cacheNodeHistogram.getAgentHistogramMap();
        Histogram cacheAgentHistogram = cacheAgentHistogramMap.get(cacheNodeAgent);
        assertEquals(cacheCallFastCount, cacheAgentHistogram.getFastCount());
        assertEquals(cacheCallSlowCount, cacheAgentHistogram.getSlowCount());
        assertEquals(cacheCallFastCount + cacheCallSlowCount, cacheAgentHistogram.getTotalCount());
    }

    /**
     * Checks histograms for user node.
     * <pre>
     *     userNode ---> wasNode (2 agents)
     * </pre>
     */
    @Test
    public void userNode() {
        // Given
        Range range = Range.between(0, 60 * 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        NodeList.Builder nodeBuilder = NodeList.newBuilder();

        // userNode : [userNode] user
        Node userNode = createNode("userNode", ServiceType.USER);
        String userNodeAgent = "user";
        nodeBuilder.addNode(userNode);
        // wasNode : [wasNode] was-1, was-2
        Node wasNode = createNode("wasNode", ServiceTypeFactory.of(1000, "WAS"));
        String wasNodeAgent1 = "was-1";
        String wasNodeAgent2 = "was-2";
        nodeBuilder.addNode(wasNode);
        NodeList nodeList = nodeBuilder.build();

        Link link = new Link(LinkDirection.OUT_LINK, userNode, wasNode, range);
        HistogramSlot fastSlot = wasNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = wasNode.getServiceType().getHistogramSchema().getNormalSlot();
        // [userNode] user -> [wasNode] was-1
        long fastCallCount = 100L;
        link.addOutLink(createLinkCallDataMap(userNodeAgent, userNode.getServiceType(), wasNodeAgent1, wasNode.getServiceType(), fastSlot, fastCallCount));
        // [userNode] user -> [wasNode] was-2
        long normalCallCount = 50L;
        link.addOutLink(createLinkCallDataMap(userNodeAgent, userNode.getServiceType(), wasNodeAgent2, wasNode.getServiceType(), normalSlot, normalCallCount));
        LinkList linkList = LinkList.of(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(timeWindow, nodeList, linkList, buildTimeoutMillis);

        NodeHistogram nodeHistogram = userNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        assertEquals(fastCallCount, applicationHistogram.getFastCount());
        assertEquals(normalCallCount, applicationHistogram.getNormalCount());
        assertEquals(fastCallCount + normalCallCount, applicationHistogram.getTotalCount());
        // verify agent-level histogram - there is none for user node
        Map<String, Histogram> databaseAgentHistogramMap = nodeHistogram.getAgentHistogramMap();
        assertThat(databaseAgentHistogramMap).isEmpty();
    }

    private Node createNode(String applicationName, ServiceType serviceType) {
        Application application = new Application(applicationName, serviceType);
        return new Node(application);
    }

    private LinkCallDataMap createLinkCallDataMap(String fromAgentId, ServiceType fromAgentServiceType, String toAgentId, ServiceType toAgentServiceType, HistogramSlot slot, long callCount) {
        long currentTimestamp = System.currentTimeMillis();
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        linkCallDataMap.addCallData(new Application(fromAgentId, fromAgentServiceType), new Application(toAgentId, toAgentServiceType), currentTimestamp, slot.getSlotTime(), callCount);
        return linkCallDataMap;
    }

    @Test
    public void appendNodeHistogram() throws InterruptedException {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1024);

        int maxCount = 100;
        @SuppressWarnings("unchecked")
        CompletableFuture<String>[] array = new CompletableFuture[maxCount];
        AtomicBoolean timeout = new AtomicBoolean(false);
        for (int i = 0; i < maxCount; i++) {
            array[i] = makeCompletableFuture(i, timeout);
        }

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(array);
        try {
            completableFuture.get(100, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            timeout.set(Boolean.TRUE);
        }
        TimeUnit.SECONDS.sleep(3);
        logger.debug("END");
    }

    private CompletableFuture<String> makeCompletableFuture(final int sleepMillis, final AtomicBoolean timeout) {
        return CompletableFuture.supplyAsync(() -> {
            if (timeout.get()) {
                logger.debug("Timeout");
                return "Timeout";
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
            logger.debug("RUN {}", sleepMillis);
            return "Completed";
        }, executor);
    }
}
