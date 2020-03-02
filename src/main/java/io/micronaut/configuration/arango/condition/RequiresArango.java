package io.micronaut.configuration.arango.condition;

import io.micronaut.configuration.arango.ArangoClient;
import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;

import java.lang.annotation.*;

/**
 * A custom requirement for ArangoDB.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
@Requires(classes = ArangoClient.class)
@Requires(property = ArangoSettings.PREFIX)
public @interface RequiresArango {
}
