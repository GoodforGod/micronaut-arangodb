package io.micronaut.configuration.arango;

import com.arangodb.serde.ArangoSerde;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * Creates default {@link ArangoSerde} based on {@link ObjectMapper}
 *
 * @author Anton Kurako (GoodforGod)
 * @since 05.05.2023
 */
@Requires(property = "arangodb.serde.enabled", value = "true", defaultValue = "true")
@Requires(classes = ObjectMapper.class)
@Requires(beans = ObjectMapper.class)
@Factory
public class ArangoSerdeFactory {

    @Bean
    @Singleton
    @Secondary
    public ArangoSerde defaultArangoSerde(ObjectMapper mapper) {
        return new ArangoSerde() {

            @Override
            public byte[] serialize(Object value) {
                try {
                    return mapper.writeValueAsBytes(value);
                } catch (IOException e) {
                    throw (value == null)
                            ? new SerializationException("Can't serialize", e)
                            : new SerializationException("Can't serialize: " + value.getClass(), e);
                }
            }

            @Override
            public <T> T deserialize(byte[] content, Class<T> clazz) {
                try {
                    return mapper.readValue(content, clazz);
                } catch (IOException e) {
                    throw new SerializationException("Can't deserialize: " + clazz, e);
                }
            }
        };
    }
}
