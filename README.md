# Micronaut ArangoDB Configuration

![Java CI](https://github.com/GoodforGod/micronaut-arangodb/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)

This project includes integration between Micronaut and ArangoDB.

## Dependency :rocket:
**Gradle**
```groovy
dependencies {
    compile 'com.github.goodforgod:micronaut-arangodb:1.1.1'
}
```

**Maven**
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>micronaut-arangodb</artifactId>
    <version>1.1.1</version>
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

Accessors injected as [**prototypes**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
beans remember that while using them.

```java
@Inject
private ArangoDBAsync async;

@Inject
private ArangoDB sync;
```

### Clients

Configuration supports setup database for your application 
(ArangoDB accessors do not require or have database config).

So in order to use database specified as per [configuration](#Configuration) inject provided Arango Clients instead.

Clients injected as [**singletons**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
beans remember that while using them.

```java
@Inject
private ArangoClient asyncClient;

@Inject
private ArangoClientSync syncClient;
```

Both clients provide as sync and async implementation and are same [accessors](#Accessors) 
but with knowledge about database specified per config.
So you can use connection with knowledge about database your app is working with.

```java
@MicronautTest
class ArangoClientTests {

    @Inject
    private ArangoClient asyncClient;    

    @Inject
    private ArangoClientSync syncClient;    

    void checkConfiguredDatabase() {
        final String databaseAsync = asyncClient.getDatabase(); // Database as per config
        final String databaseSync = syncClient.getDatabase(); // Database as per config
        assertEquals(database, database);
    
        final ArangoDBAsync async = asyncClient.accessor();
        final ArangoDB sync = syncClient.accessor();
    }
}
```

In case you want inject clients as [**prototypes**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
you can use *named* bean injection.

```java
@Named("prototype")
@Inject
private ArangoClient asyncClient;    

@Named("prototype")
@Inject
private ArangoClientSync syncClient;    
```

### Configuring ArangoDB Driver

All accessors and clients are provided as [**refreshable**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) with *arangodb* key for bean refresh.

Configuration supports all available ArangoDB driver settings.

Configuring timeout, chunksize, maxConnections, connectionTtl, acquireHostList, loadBalancingStrategy for *clients & accessors*

Check [ArangoDB official](https://www.arangodb.com/docs/stable/drivers/java-reference-setup.html) info about each parameter.

```yaml
arangodb:
  timeout: 3000                         # default - 0 in milliseconds
  chunksize: 3000                       # default - 30000
  useSsl: true                          # default - false
  maxConnections: 30                    # default - 1
  connectionTtl: 200                    # default - null
  acquireHostList: true                 # default - false
  loadBalancingStrategy: ONE_RANDOM     # default - NONE (check LoadBalancingStrategy for more)
```

#### Database Initialization

There is an option to initialize database if it doesn't exist on startup via *createDatabaseIfNotExist* option.

Usage:

```yaml
arangodb:
  createDatabaseIfNotExist: true    # default - false
```

### Health Check

Health check for ArangoDB is provided and is *turned on* by default.

ArangoDB health check is part of [Micronaut Health Endpoint](https://docs.micronaut.io/latest/guide/index.html#healthEndpoint).

Example of ArangoDB health:

```json
{
  "name": "service",
  "status": "UP",
  "details": {
    "arangodb": {
      "name": "arangodb",
      "status": "UP",
      "details": {
        "version": "3.6.1",
        "database": "_system"
      }
    }
  }
}
```

Where database *version* is specified and *database* name service is connected to as per [configuration](#Configuration).

You can explicitly *turn off* health check.

```yaml
arangodb:
  health:
    enabled: false      # default - true 
```

#### Cluster Health Check

There is also available ArangoDB Cluster Health Check that monitors cluster health 
(if service is connected to *cluster ArangoDB*)
and reports is *nodes* that can **not be deleted** according to [documentation](https://www.arangodb.com/docs/stable/http/cluster-health.html) from *cluster are down*, so *application is also down*.

In other case application will be *UP* and running with errors like various connection issues due to unstable cluster.

**Both health checks** will be present in health output if all are enabled.

ArangoDB Cluster Health output example:

```json
{
  "name": "service",
  "status": "UP",
  "details": {
    "arangodb (cluster)": {
      "name": "arangodb (cluster)",
      "status": "UP",
      "details": {
        "clusterId": "89b7e1a8-53f5-44ea-bb5c-9e7cb201417c",
        "nodes": [
          {
            "name": "Coordinator0002",
            "status": "GOOD"
          },
          {
            "name": "Coordinator0001",
            "status": "GOOD"
          },
          {
            "name": "DBServer0001",
            "status": "GOOD"
          },
          {
            "name": "DBServer0002",
            "status": "GOOD"
          },
          {
            "name": "AGENT (AGNT-4206f181-8791-4c3f-952d-79d9aa58b7c2)",
            "leading": true,
            "status": "GOOD"
          },
          {
            "name": "AGENT (AGNT-2cc832bf-a8b7-4f8a-823a-15d778594bc1)",
            "status": "GOOD"
          },
          {
            "name": "AGENT (AGNT-78172be0-1adc-47ea-8152-3a3b0ab0b10e)",
            "status": "GOOD"
          }
        ]
      }
    }
  }
}
```

HealthCheck provides status of each node in cluster and their *ShortName* for [DBServer and Coordinator](https://www.arangodb.com/docs/stable/http/cluster-health.html)
or *NodeID* for *Agent* nodes and flag for leading *Agent* node.

You can turn on Cluster Health Check via configuration:

```yaml
arangodb:
  health:
    cluster:
      enabled: true      # default - false
```

## Testing

For testing purposes it is recommended to use [ArangoDB TestContainer library](https://github.com/GoodforGod/arangodb-testcontainer) 
(this project is tested via that library). 

TestContainers allows you to use integration tests with real database in all docker friendly environments, 
check here for [TestContainers](https://www.testcontainers.org/).

## Version History

**1.1.0** - Client as prototype injection added, health indicators accessors instead of clients, sync client renamed.

**1.0.0** - Initial version, sync and async clients and drivers injection, database initialization, health check, cluster health check.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
