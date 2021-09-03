package io.micronaut.configuration.arango.health;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 03.09.2021
 */
class ArangoHealthModelsTests extends Assertions {

    @Test
    void healthClusterParsedCorrectly() throws JsonProcessingException {
        try (final ApplicationContext context = ApplicationContext.run()) {
            final String json = getFromResource("/data/cluster-health.json");
            final ObjectMapper mapper = context.getBean(ObjectMapper.class);

            final ClusterHealthResponse response = mapper.readValue(json, ClusterHealthResponse.class);
            assertNotNull(response);
            assertNotNull(response.getClusterId());
            assertNotNull(response.getNodes());
            assertFalse(response.isError());

            response.getNodes().forEach((k, v) -> {
                assertNotNull(k);
                assertNotNull(v);
                assertNotNull(v.getEndpoint());
                assertNotNull(v.getHealthStatus());
                assertNotNull(v.getEndpoint());
                assertNotNull(v.getEngine());
                assertNotNull(v.getRoleWithNodeId());
                assertNotNull(v.getNodeRole());
                assertNotNull(v.getVersion());
            });
        }
    }

    private String getFromResource(String path) {
        try {
            return new String(getClass().getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
