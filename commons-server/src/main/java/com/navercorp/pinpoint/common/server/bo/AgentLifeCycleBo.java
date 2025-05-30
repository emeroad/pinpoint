/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.server.util.ByteUtils;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AgentLifeCycleBo {

    public static final int CURRENT_VERSION = 0;

    private final byte version;
    @NotBlank private final String agentId;
    @PositiveOrZero private final long startTimestamp;
    @PositiveOrZero private final long eventTimestamp;
    private final long eventIdentifier;
    private final AgentLifeCycleState agentLifeCycleState;
    
    public AgentLifeCycleBo(String agentId, long startTimestamp, long eventTimestamp, long eventIdentifier, AgentLifeCycleState agentLifeCycleState) {
        this(CURRENT_VERSION, agentId, startTimestamp, eventTimestamp, eventIdentifier, agentLifeCycleState);
    }

    public AgentLifeCycleBo(int version, String agentId, long startTimestamp, long eventTimestamp, long eventIdentifier, AgentLifeCycleState agentLifeCycleState) {
        this.version = ByteUtils.toUnsignedByte(version);
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        if (startTimestamp < 0) {
            throw new IllegalArgumentException("startTimestamp cannot be less than 0");
        }
        if (eventTimestamp < 0) {
            throw new IllegalArgumentException("eventTimestamp cannot be less than 0");
        }
        if (eventIdentifier < 0) {
            throw new IllegalArgumentException("eventIdentifier cannot be less than 0");
        }
        this.startTimestamp = startTimestamp;
        this.eventTimestamp = eventTimestamp;
        this.eventIdentifier = eventIdentifier;
        this.agentLifeCycleState = Objects.requireNonNull(agentLifeCycleState, "agentLifeCycleState");
    }

    public int getVersion() {
        return Byte.toUnsignedInt(this.version);
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public long getEventIdentifier() {
        return this.eventIdentifier;
    }

    public AgentLifeCycleState getAgentLifeCycleState() {
        return this.agentLifeCycleState;
    }

    @Override
    public String toString() {
        return "AgentLifeCycleBo{" +
                "version=" + version +
                ", agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", eventTimestamp=" + eventTimestamp +
                ", eventIdentifier=" + eventIdentifier +
                ", agentLifeCycleState=" + agentLifeCycleState +
                '}';
    }
}
