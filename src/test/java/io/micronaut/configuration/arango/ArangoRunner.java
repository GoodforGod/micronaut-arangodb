package io.micronaut.configuration.arango;

import io.testcontainers.arangodb.containers.ArangoContainer;
import org.junit.Assert;
import org.testcontainers.containers.Network;

import java.util.Arrays;
import java.util.List;

/**
 * Description in progress
 *
 * @author Anton Kurako (GoodforGod)
 * @since 11.3.2020
 */
public abstract class ArangoRunner extends Assert {

    @SuppressWarnings("rawtypes")
    public static List<ArangoContainer> getCluster() {
        final Network network = Network.newNetwork();

        final String agencyAlias = "agency_1";
        final String coordinator1Alias = "coordinator_1";
        final String coordinator2Alias = "coordinator_2";
        final String db2Alias = "db_1";
        final String db1Alias = "db_2";

        final ArangoContainer agency = (ArangoContainer) new ArangoContainer().withoutAuthentication()
                .withNetwork(network)
                .withNetworkAliases(agencyAlias);
        agency.setCommand("arangod --server.endpoint tcp://0.0.0.0:5001 --server.authentication false --agency.activate true --agency.size 1 --agency.supervision true --database.directory /var/lib/arangodb3/agency1");

        final ArangoContainer coordinator1 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
                .setPort(8000)
                .dependsOn(agency)
                .withNetwork(network)
                .withNetworkAliases(coordinator1Alias);
        coordinator1.setCommand("arangod --server.authentication=false --server.endpoint tcp://0.0.0.0:8529 --cluster.my-address tcp://coordinator:8529 --cluster.my-local-info "+ db1Alias +"  --cluster.my-role COORDINATOR --cluster.agency-endpoint tcp://" + agencyAlias + ":5001   --database.directory /var/lib/arangodb3/coordinator");

        final ArangoContainer coordinator2 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
                .setPort(8001)
                .dependsOn(agency)
                .withNetwork(network)
                .withNetworkAliases(coordinator2Alias);
        coordinator2.setCommand("arangod --server.authentication=false --server.endpoint tcp://0.0.0.0:8529 --cluster.my-address tcp://coordinator2:8529 --cluster.my-local-info "+ db1Alias +"  --cluster.my-role COORDINATOR --cluster.agency-endpoint tcp://"+ agencyAlias +":5001   --database.directory /var/lib/arangodb3/coordinator");

        final ArangoContainer db1 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
                .dependsOn(agency)
                .withNetwork(network)
                .withNetworkAliases(db2Alias);
        db1.setCommand("arangod --server.authentication=false --server.endpoint tcp://0.0.0.0:8529 --cluster.my-address tcp://db1:8529 --cluster.my-local-info " + db1Alias + " --cluster.my-role PRIMARY --cluster.agency-endpoint tcp://"+ agencyAlias +":5001  --database.directory /var/lib/arangodb3/primary1");

        final ArangoContainer db2 = (ArangoContainer) new ArangoContainer().withoutAuthentication()
                .dependsOn(agency)
                .withNetworkAliases(db1Alias);
        db2.setCommand("arangod --server.authentication=false --server.endpoint tcp://0.0.0.0:8529 --cluster.my-address tcp://db2:8529 --cluster.my-local-info " + db1Alias + " --cluster.my-role PRIMARY --cluster.agency-endpoint tcp://"+ agencyAlias +":5001  --database.directory /var/lib/arangodb3/primary1");

        return Arrays.asList(agency, coordinator1, coordinator2, db1, db2);
    }
}
