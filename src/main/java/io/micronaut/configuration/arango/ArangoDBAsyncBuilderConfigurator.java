package io.micronaut.configuration.arango;

import com.arangodb.async.ArangoDBAsync;

import java.util.function.Consumer;

public interface ArangoDBAsyncBuilderConfigurator extends Consumer<ArangoDBAsync.Builder> {
}
