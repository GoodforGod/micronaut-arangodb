package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Arango node health info
 *
 * @author Anton Kurako
 * @see HealthCluster#getNodes()
 * @since 07.02.2020
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
}
