package org.osiam.tests.performance;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PerformanceTestContext {
    public static final String VALID_USER_ID = "be108952-0667-42a9-864e-3e1012f1234a";
    public static final String VALID_GROUP_ID = "0c24ab45-ae2a-43fe-8e9b-37581175c87c";
    
    private static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    private static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    private static final String CLIENT_ID = "example-client";
    private static final String CLIENT_SECRET = "secret";
    
    private static OsiamConnector osiamConnector;
    private static AccessToken accessToken;

    @BeforeClass
    public static void setupDB() {
        System.out.println("Setting up DB");
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml")) {
            IDatabaseConnection connection = new DatabaseDataSourceConnection(
                    (DataSource) applicationContext.getBean("dataSource"));

            try {
                DatabaseOperation.CLEAN_INSERT.execute(connection,
                        new FlatXmlDataSetBuilder().build(
                                applicationContext.getResource("/database_seed.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Setting up Connector");
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        osiamConnector = oConBuilder.build();

        System.out.println("Retrieving access token");
        accessToken = osiamConnector.retrieveAccessToken();
    }

    @AfterClass
    public static void tearDownDB() {
        System.out.println("Tearing down DB");
        try (ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("context.xml")) {
            IDatabaseConnection connection = new DatabaseDataSourceConnection(
                    (DataSource) applicationContext.getBean("dataSource"));

            try {
                DatabaseOperation.DELETE_ALL.execute(connection,
                        new FlatXmlDataSetBuilder().build(
                                applicationContext.getResource("/database_tear_down.xml").getInputStream()));
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static OsiamConnector getOsiamConnector() {
        return osiamConnector;
    }

    public static AccessToken getAccessToken() {
        return accessToken;
    }

    @Test
    @Ignore("Needed for JMeter to run @BeforeClass and @AfterClass methods")
    public void dummy() {
    }

}