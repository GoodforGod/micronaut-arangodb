package io.micronaut.configuration.arango;

/**
 * Common constants to for ArangoDB settings.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 1.3.2020
 */
public final class ArangoSettings {

    private ArangoSettings() {}

    /**
     * Prefix to use for all ArangoDB settings.
     */
    public static final String PREFIX = "arangodb";

    /**
     * ArangoDB default database name
     */
    public static final String SYSTEM_DATABASE = "_system";
}
