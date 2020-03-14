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
    @JsonProperty("Leader")
    private String leader;
    @JsonProperty("Status")
    private String status;

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

    public HealthStatus getHealthStatus() {
        if (isEmpty(status))
            return UNKNOWN;

        switch (status) {
            case "GOOD":
                return UP;
            case "FAILED":
                return DOWN;
            default:
                return UNKNOWN;
        }
    }
}
