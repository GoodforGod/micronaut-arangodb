package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.health.HealthStatus;

import static io.micronaut.core.util.StringUtils.isEmpty;
import static io.micronaut.health.HealthStatus.*;
import static io.micronaut.health.HealthStatus.UNKNOWN;

/**
 * A HealthNode DTO for ArangoDB {@link HealthCluster} health check information.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.3.2020
 */
public class HealthNode {

    public enum NodeRole {
        AGENT,
        COORDINATOR,
        DBSERVER,
        UNKNOWN
    }

    private String nodeId;
    @JsonProperty("ShortName")
    private String shortName;
    @JsonProperty("Endpoint")
    private String endpoint;
    @JsonProperty("Role")
    private String role;

    @JsonProperty("CanBeDeleted")
    private boolean canBeDeleted;

    @JsonProperty("Host")
    private String host;
    @JsonProperty("Version")
    private String version;
    @JsonProperty("Engine")
    private String engine;
    @JsonProperty("Status")
    private String status;

    @JsonProperty("Leader")
    private String leader;
    @JsonProperty("Leading")
    private boolean isLeading;

    public String getRoleWithNodeId() {
        return isEmpty(nodeId)
                ? role
                : role + " (" + nodeId + ")";
    }

    public String getNodeId() {
        return nodeId;
    }

    public HealthNode setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public Boolean isLeading() {
        return isLeading;
    }

    public String getShortName() {
        return shortName;
    }

    public String getHost() {
        return host;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRole() {
        return role;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public String getVersion() {
        return version;
    }

    public String getEngine() {
        return engine;
    }

    public String getLeader() {
        return leader;
    }

    public String getStatus() {
        return status;
    }

    public NodeRole getNodeRole() {
        if (isEmpty(role))
            return NodeRole.UNKNOWN;

        switch (role) {
            case "AGENT":
                return NodeRole.AGENT;
            case "COORDINATOR":
                return NodeRole.COORDINATOR;
            case "DBSERVER":
                return NodeRole.DBSERVER;
            default:
                return NodeRole.UNKNOWN;
        }
    }

    public HealthStatus getHealthStatus() {
        if (isEmpty(status))
            return UNKNOWN;

        switch (status) {
            case "GOOD":
            case "BAD":
                return UP;
            case "FAILED":
                return DOWN;
            default:
                return UNKNOWN;
        }
    }
}
