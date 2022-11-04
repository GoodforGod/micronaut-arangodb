package io.micronaut.configuration.arango;

import com.arangodb.ArangoDB;
import com.arangodb.async.ArangoDBAsync;

import java.util.function.Consumer;

public interface ArangoDBBuilderConfigurator extends Consumer<ArangoDB.Builder> {
}
