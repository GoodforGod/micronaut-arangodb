package io.micronaut.configuration.arango.condition;

import io.micronaut.configuration.arango.ArangoSettings;
import io.micronaut.context.annotation.Requires;
import java.lang.annotation.*;

/**
 * A custom requirement for ArangoDB.
 *
 * @author Anton Kurako (GoodforGod)
 * @since 2.3.2020
 */
@Requires(property = ArangoSettings.PREFIX)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PACKAGE, ElementType.TYPE })
public @interface RequiresArango {
}
