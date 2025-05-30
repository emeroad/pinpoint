package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;

import java.util.Objects;
import java.util.function.Supplier;

public class BindAttribute {
    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final Supplier<ApplicationUid> applicationUid;


    private final long agentStartTime;
    private final long acceptedTime;

    public BindAttribute(String agentId,
                         String agentName,
                         String applicationName,
                         Supplier<ApplicationUid> applicationUid,
                         long agentStartTime,
                         long acceptedTime) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.agentName = agentName;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.applicationUid = Objects.requireNonNull(applicationUid, "applicationUid");
        this.agentStartTime = agentStartTime;
        this.acceptedTime = acceptedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public Supplier<ApplicationUid> getApplicationUid() {
        return applicationUid;
    }

    public long getAgentStartTime() {
        return this.agentStartTime;
    }
}
