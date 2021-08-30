package io.micronaut.configuration.arango;

/**
 * Common constants to for ArangoDB settings.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.3.2020
 */
public interface ArangoSettings {

    /**
     * Prefix to use for all ArangoDB settings.
     */
    String PREFIX = "arangodb";

    /**
     * ArangoDB default database name
     */
    String SYSTEM_DATABASE = "_system";
}
