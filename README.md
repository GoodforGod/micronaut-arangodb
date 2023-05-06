# Micronaut ArangoDB Configuration

[![Minimum required Java version](https://img.shields.io/badge/Java-11%2B-blue?logo=openjdk)](https://openjdk.org/projects/jdk/11/)
![Java CI](https://github.com/GoodforGod/micronaut-arangodb/workflows/Java%20CI/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=alert_status)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_micronaut-arangodb&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_micronaut-arangodb)

This project includes integration between Micronaut and ArangoDB.

## Dependency :rocket:

[**Gradle**](https://mvnrepository.com/artifact/com.github.goodforgod/micronaut-arangodb)
```groovy
implementation "com.github.goodforgod:micronaut-arangodb:4.0.0"
```

[**Maven**](https://mvnrepository.com/artifact/com.github.goodforgod/micronaut-arangodb)
```xml
<dependency>
    <groupId>com.github.goodforgod</groupId>
    <artifactId>micronaut-arangodb</artifactId>
    <version>4.0.0</version>
</dependency>
```

## Configuration

Includes a configuration to automatically configure the native [ArangoDB Java drive](https://github.com/arangodb/arangodb-java-driver). 
Just configure the host, port, credentials (if needed) of the ArangoDB accessor in *application.yml*.

```yaml
arangodb:
  host: localhost     # default
  port: 8529          # default
  database: _system   # default (is used for health check)
  user: root          # default
  password: 1234      # or no pass if auth is not required
```

### Accessors

*ArangoDB* accessor is available for dependency injection.

Accessors injected as [**singleton**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
beans remember that while using them.

```java
@Inject
private ArangoDB accessor;
```

In case you want inject clients as [**prototypes**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes)
you can use *named* bean injection.

```java
@Named("prototype")
@Inject
private ArangoDB accessor;    
```

### ArangoSerde

You can provide custom *ArangoSerde* serialization module as bean, and it will be used while building ArangoDB accessor or client.

ArangoSerialization factory example:
```java
@Factory
public class ArangoSerdeFactory {

    @Bean
    public ArangoSerde getArangoSerde() {
        return new JacksonSerdeImpl(new ObjectMapper());
    }
}
```

### Clients

Configuration supports setup database for your application 
(ArangoDB accessors do not require or have database config).

In order to use database specified as per [configuration](#Configuration) inject provided Arango Clients instead.

Clients injected as [**singletons**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
beans remember that while using them.

```java
@Inject
private ArangoClient client;
```

Both clients provide as sync and async implementation and are same [accessors](#Accessors) 
but with knowledge about database specified per config.
So you can use connection with knowledge about database your app is working with.

```java
@MicronautTest
class ArangoClientTests {

    @Inject
    private ArangoClient client;    

    void checkConfiguredDatabase() {
        final String databaseSync = client.getDatabase(); // Database as per config
        assertEquals(database, database);
    }
}
```

In case you want inject clients as [**prototypes**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) 
you can use *named* bean injection.

```java
@Named("prototype")
@Inject
private ArangoClient client;    
```

### Configuring ArangoDB Driver

All accessors and clients are provided as [**refreshable**](https://docs.micronaut.io/latest/guide/index.html#builtInScopes) with *arangodb* key for bean refresh.

Configuration supports all available ArangoDB driver settings.

Configuring timeout, chunksize, maxConnections, connectionTtl, acquireHostList, loadBalancingStrategy for *clients & accessors*

Check [ArangoDB official](https://www.arangodb.com/docs/stable/drivers/java-reference-setup.html) info about each parameter.

```yaml
arangodb:
  hosts: localhost:8080,localhost:8081    # default - null
  user: user                              # default - root
  password: password                      # default - null
  database: _system                       # default - _system
  protocol: HTTP2_JSON                    # default - HTTP2_JSON
  timeout: 10000ms                        # default - 10000 in milliseconds
  jwt: YourToken                          # default - null
  chunksize: 3000                         # default - 30000
  connection-max: 30                      # default - 1
  connection-ttl: 200                     # default - null
  verify-host: true                       # default - true
  keep-alive-interval: 200                # default - null
  acquire-host-list: true                 # default - false
  acquire-host-list-interval: 360000      # default - 3600000 (hour)
  load-balancing-strategy: ONE_RANDOM     # default - NONE (check LoadBalancingStrategy for more)
  response-queue-time-samples: 10         # default - 10
```

Hosts can be passed to configuration as Strings (useful when passed via environment):

```yaml
arangodb:
  hosts: localhost:8080,localhost:8081    # default to host - localhost:8080
```

Or can be passed as list (useful for manual configuring):

```yaml
arangodb:
  hosts:
    - localhost:8080
    - localhost:8081
```

#### Configuring SSL

Configured SSLContext for ArangoDB driver.

Check for [more info](https://www.arangodb.com/docs/stable/programs-arangod-ssl.html).

```yaml
arangodb:
  ssl: 
    enabled: true                       # default - false
    certificate:
      enabled: true
      value:                            # certificate as base64
      alias: arangodb
      type: X.509
      algorithm: PKIX
      key-store: jks
      protocol: TLS
```

#### Database Initialization

There is an option to initialize database if it doesn't exist on startup via *createDatabaseIfNotExist* option.

Use this option if your service is lazy initialized, to set up database for [HealthCheck](#health-check).

```yaml
arangodb:
  create-database-if-not-exist: true    # default - false
```

Default timeout for operation set to 10000 millis, if you want to specify timeout *in seconds* for database creation
on startup you can set it via property.

```yaml
arangodb:
  create-database-timeout: 10000ms      # default - 10000
```

In case you want to create database asynchronously you can specify that via this property:
```yaml
arangodb:
  create-database-async: true           # default - false
```

### Micronaut Serialization

Library support by default [Micronaut Serialization](https://micronaut-projects.github.io/micronaut-serialization) module if found on classpath, 
please check Micronaut documentation on how to configure it and use it.

In case you would like to disable [Micronaut Serialization](https://micronaut-projects.github.io/micronaut-serialization) for ArangoDB module only, use option below:
```yaml
arangodb:
  serde:
    enabled: true     # default - true
```

### Health Check

Health check for ArangoDB is provided and is *turned on* by default.
HeathCheck is active for database that is specified in [configuration](#configuration).

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
        "version": "3.7.13",
        "database": "_system"
      }
    }
  }
}
```

Where database *version* is specified and *database* name service is connected to as per [configuration](#Configuration).

You can explicitly *turn off* health check.

```yaml
endpoints:
  health:
    arangodb:
      enabled: true             # default - true 
      timeout: 5000ms           # default - 5000
      retry: 2                  # default - 2
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
  "name": "service-name",
  "status": "DOWN",
  "details": {
    "arangodb-cluster": {
      "name": "service-name",
      "status": "DOWN",
      "details": {
        "clusterId": "752f578b-8884-47ef-8984-894ae110d259",
        "version": "3.7.13",
        "database": "_system",
        "cluster": [
          {
            "status": "UP",
            "nodes": [
              "Coordinator0002",
              "DBServer0002",
              "DBServer0001",
              "Agent",
              "Agent Leader",
              "Agent"
            ]
          },
          {
            "status": "DOWN",
            "nodes": [
              "Coordinator0001"
            ]
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
endpoints:
  health:
    arangodb:
      cluster:
        enabled: false            # default - false 
        timeout: 5000ms           # default - 5000
        retry: 2                  # default - 2
```

## Testing

For testing purposes it is recommended to use [ArangoDB TestContainer library](https://github.com/GoodforGod/arangodb-testcontainer) 
(this project tested via that library). 

TestContainers allows you to use integration tests against real database in all docker friendly environments, 
check here for [TestContainers](https://www.testcontainers.org/).

## Micronaut Compatability

Starting from version *3.0.0* library ships for *Micronaut 3*.

Starting from version *2.0.0* library ships for *Micronaut 2*.

Starting from version *2.1.0* Java 11+ is required (previous version 1.8+ compatible).

Last release for **Micronaut 1** is [version *1.2.1*](https://github.com/GoodforGod/micronaut-arangodb/releases/tag/v1.2.1).

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
