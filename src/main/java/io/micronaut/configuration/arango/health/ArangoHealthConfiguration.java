package io.micronaut.configuration.arango.health;

import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

/**
 * @author Anton Kurako (GoodforGod)
 * @since 13.08.2021
 */
@Requires(property = ArangoSettings.PREFIX)
@ConfigurationProperties("endpoints.health.arangodb")
public class ArangoHealthConfiguration extends AbstractHealthConfiguration {

}
