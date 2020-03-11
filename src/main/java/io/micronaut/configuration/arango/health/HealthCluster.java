package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * A ArangoDB cluster health DTO object.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.3.2020
 */
public class HealthCluster {

    @JsonProperty("Health")
    private Map<String, HealthNode> nodes;
    @JsonProperty("ClusterId")
    private String clusterId;
    private boolean error;
    private int code;

    public Map<String, HealthNode> getNodes() {
        return nodes;
    }

    public String getClusterId() {
        return clusterId;
    }

    public boolean isError() {
        return error;
    }

    public int getCode() {
        return code;
    }
}
