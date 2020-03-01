package io.micronaut.configuration.arango;

/**
 * Description in progress
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
     * ArangoDB host setting.
     */
    String ARANGO_HOST = PREFIX + ".host";

    /**
     * ArangoDB port setting.
     */
    String ARANGO_PORT = PREFIX + ".port";

    /**
     * ArangoDB database setting.
     */
    String ARANGO_DATABASE = PREFIX + ".database";

    /**
     * ArangoDB user setting.
     */
    String ARANGO_USER = PREFIX + ".user";

    /**
     * ArangoDB password setting.
     */
    String ARANGO_PASSWORD = PREFIX + ".password";

    /**
     * ArangoDB default DATABASE
     * 
     * @see #ARANGO_PORT
     */
    String DEFAULT_DATABASE = "_system";
}
