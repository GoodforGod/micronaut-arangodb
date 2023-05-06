package io.micronaut.configuration.arango.health;

import static io.micronaut.core.util.StringUtils.isEmpty;
import static io.micronaut.health.HealthStatus.DOWN;
import static io.micronaut.health.HealthStatus.UNKNOWN;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.health.HealthStatus;

/**
 * A HealthNode DTO for ArangoDB {@link ClusterHealthResponse} health check
 * information.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.3.2020
 */
final class ClusterHealthNode {

    private static final String ROLE_LEADER = "Agent Leader";

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
                ? getNodeRole().name()
                : getNodeRole().name() + " (" + nodeId + ")";
    }

    public String getNodeId() {
        return nodeId;
    }

    public ClusterHealthNode setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public boolean isLeading() {
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
        return isLeading()
                ? ROLE_LEADER
                : role;
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
        if ("AGENT".equalsIgnoreCase(role))
            return NodeRole.AGENT;
        else if ("COORDINATOR".equalsIgnoreCase(role))
            return NodeRole.COORDINATOR;
        else if ("DBSERVER".equalsIgnoreCase(role))
            return NodeRole.DBSERVER;
        else
            return NodeRole.UNKNOWN;
    }

    public HealthStatus getHealthStatus() {
        if ("GOOD".equalsIgnoreCase(status))
            return HealthStatus.UP;
        else if ("BAD".equalsIgnoreCase(status))
            return DOWN;
        else
            return UNKNOWN;
    }
}
