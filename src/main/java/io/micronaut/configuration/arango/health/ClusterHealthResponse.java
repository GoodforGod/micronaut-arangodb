package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.stream.Stream;

/**
 * A ArangoDB cluster health DTO object.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 09.3.2020
 */
class ClusterHealthResponse {

    @JsonProperty("Health")
    private Map<String, ClusterHealthNode> nodes;
    @JsonProperty("ClusterId")
    private String clusterId;
    private boolean error;
    private int code;

    public Map<String, ClusterHealthNode> getNodes() {
        return nodes;
    }

    @JsonIgnore
    public Stream<ClusterHealthNode> streamNodes() {
        return nodes.entrySet().stream().map(e -> e.getValue().setNodeId(e.getKey()));
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
