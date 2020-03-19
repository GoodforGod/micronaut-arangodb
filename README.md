# Micronaut ArangoDB Configuration

![Java CI](https://github.com/GoodforGod/arangodb-testcontainer/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_arangodb-testcontainer&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_arangodb-testcontainer)


This project includes integration between Micronaut and ArangoDB.

## Dependency :rocket:
**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:micronaut-arangodb:1.0.0'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>micronaut-arangodb</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Configuration

Includes a configuration to automatically configure the native [ArangoDB Java drive](https://github.com/arangodb/arangodb-java-driver). 
Just configure the host, port, credentials (if needed) of the ArangoDB accessor in *application.yml*.

```yaml
arangodb:
  host: localhost     # default
  port: 8529          # default
  database: _system   # default
  user: root          # default
  password: 1234      # or no pass if auth is not required
```

To use the drivers, just add a dependency to your application.

```groovy
compile 'com.arangodb:arangodb-java-driver'
```

### Accessors

Both async *ArangoDBAsync* and sync *ArangoDB* accessors are then available for dependency injection.

Accessors are injected as [**prototypes**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) remember that while using them.

```java
@Inject
private ArangoDBAsync async;

@Inject
private ArangoDBAsync sync;
```

### Clients

Configuration supports to setup database for your application 
(ArangoDB accessors do not require or have database config).

So in order to use database specified as per [configuration](#Configuration) inject provided Arango Clients instead.

Clients are injected as [**singletons**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) remember that while using them.

```java
@Inject
private ArangoClient asyncClient;

@Inject
private ArangoSyncClient syncClient;
```

Both clients provides as sync and async implementation and are same [accessors](#Accessors) 
but with knowledge about database specified per config.
So you can use connection with knowledge about database your app is working with.

```java
@MicronautTest
class ArangoClientTests {

    @Inject
    private ArangoClient asyncClient;    

    @Inject
    private ArangoSyncClient syncClient;    

    void checkConfiguredDatabase() {
        final String databaseAsync = asyncClient.getDatabase(); // Database as per config
        final String databaseSync = syncClient.getDatabase(); // Database as per config
        assertEquals(database, database);
    
        final ArangoDBAsync async = asyncClient.accessor();
        final ArangoDB sync = syncClient.accessor();
    }
}
```

### Configuring ArangoDB Driver

All accessors and clients are provided as [**refreshable**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) with *arangodb* key for bean refresh.

Configuration supports all available ArangoDB driver settings.

Configuring timeout, chunksize, maxConnections, connectionTtl, acquireHostList, loadBalancingStrategy for *clients & accessors*

Check [ArangoDB official](https://www.arangodb.com/docs/stable/drivers/java-reference-setup.html) info about each parameter.
```yaml
arangodb:
  timeout: 3000                   # default - 0 in milliseconds
  chunksize: 3000                 # default - 30000
  useSsl: true                    # default - false
  maxConnections: 30              # default - 1
  connectionTtl: 200              # default - null
  acquireHostList: true           # default - false
  loadBalancingStrategy: 1234     # default - NONE (check LoadBalancingStrategy for more)
```

#### Database Initialization

### Health Check

#### Cluster Health Check

## Testing

## Version History

**1.0.0** - Initial version, sync and async clients and drivers injection, database initialization, health check, cluster health check.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
