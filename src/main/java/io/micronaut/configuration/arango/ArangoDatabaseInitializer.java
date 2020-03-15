package io.micronaut.configuration.arango;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 16.3.2020
 */
@Requires(beans = ArangoClient.class)
@Context
public class ArangoDatabaseInitializer {
}
