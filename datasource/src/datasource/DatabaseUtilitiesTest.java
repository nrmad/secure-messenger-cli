package datasource;


import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

public class DatabaseUtilitiesTest {

    private static DatabaseUtilities databaseUtilities;

    @Before
    public void setUp() {
        try {
            DatabaseUtilities.setDatabaseUtilities("relay-app", "relaypass");
            databaseUtilities = DatabaseUtilities.getInstance();
        }catch(SQLException | IOException e){
            System.out.println(e.getMessage());
        }
    }

    @After
    public void tearDown() {

        databaseUtilities.tempMethod();
        databaseUtilities.closeConnection();

    }

    @org.junit.Test
    public void setDatabaseUtilities() {
            assertNotNull(databaseUtilities);
    }

    @org.junit.Test
    public void getInstance() {
        try {
            assertNotNull(DatabaseUtilities.getInstance());

        }catch (SQLException e){}
    }

    @org.junit.Test
    public void updateNetworkPorts(){

        assertFalse(databaseUtilities.updateNetworkPorts(true, 1000));

        // CODE TO ADD NETWORKS SO UPDATES CAN BE SUCCESSFUL

        assertTrue(databaseUtilities.updateNetworkPorts(true, 5000));
        assertTrue(databaseUtilities.updateNetworkPorts(false, 6000));

    }

    @org.junit.Test
    public void updateNetworkAliases(){
        List<Network> networks = new ArrayList<>();
        networks.add(new Network(2,"james"));
        assertFalse(databaseUtilities.updateNetworkAliases(networks));
        networks.clear();
        networks.add(new Network(1, "NOT_REG"));
        assertFalse(databaseUtilities.updateNetworkAliases(networks));

        // CODE TO ADD NETWORKS SO UPDATES CAN BE SUCCESSFUL

        networks.clear();
        networks.add(new Network(2,  "tom"));
        networks.add(new Network(3, "dick" ));
        networks.add(new Network(4, "harry"));
        databaseUtilities.addNetworks(networks);

        for(Network network: networks){
            network.setNetworkAlias(network.getNetworkAlias()+"boi");
        }

        assertTrue(databaseUtilities.updateNetworkAliases(networks));
    }


    @org.junit.Test
    public void getAllNetworks(){

        List<Network> networks = new ArrayList<>(), networks1;
        // call get with no networks and assert error

        // add a bunch of networks then call get and assert true that it returns
        try {
            networks.add(new Network(2,  "tom"));
            networks.add(new Network(3, "dick"));
            networks.add(new Network(4,  "harry"));
            databaseUtilities.addNetworks(networks);

            networks1 = databaseUtilities.getAllNetworks();
            for(int i = 0; i< networks.size(); i++){
                assertEquals(networks.get(i).getNid(), networks1.get(i+1).getNid());
                assertEquals(networks.get(i).getNetworkAlias(), networks1.get(i+1).getNetworkAlias());
            }
        }catch (SQLException e){
            fail("failed it");
        }
    }

    @org.junit.Test
    public void addNetworks(){

        List<Network> networks = new ArrayList<>();
        // ACTUAL USAGE DOES NOT INCLUDE MANUAL NID BUT DOESN'T MATTER
        networks.add(new Network(1,  "tom"));
        networks.add(new Network(2, "dick"));
        networks.add(new Network(3, "harry"));
        assertTrue(databaseUtilities.addNetworks(networks));
        networks.clear();

        networks.add(new Network(4,  "harry"));
        assertFalse(databaseUtilities.addNetworks(networks));
        networks.clear();

        networks.add(new Network(4,  ""));
        assertFalse(databaseUtilities.addNetworks(networks));
        networks.clear();



        networks.clear();

    }

//    @org.junit.Test
//    public void getNetworks(){
//
//        List<Network> networks = new ArrayList<>(), networks1;
//        networks.add(new Network(2,  "tom"));
//        networks.add(new Network(3, "dick"));
//        networks.add(new Network(4,  "harry"));
//        databaseUtilities.addNetworks(networks);
//
//
//        try {
//            networks1 = databaseUtilities.getNetworks(networks.subList(0, 1));
//
//            for (int i = 0; i < networks1.size(); i++) {
//                assertEquals(networks.get(i).getNid(), networks1.get(i).getNid());
//                assertEquals(networks.get(i).getNetworkAlias(), networks1.get(i).getNetworkAlias());
//            }
//        }catch (SQLException e) {
//        fail("should have reached");
//        }
//
//        try{
//            databaseUtilities.getNetworks(new ArrayList<>(Arrays.asList(new Network(5,"jimmy"))));
//            fail("shouldn't reach");
//        }catch (SQLException e){
//         assertTrue(true);
//        }
//    }

    @org.junit.Test
    public void deleteNetworks(){

        List<Network> networks = new ArrayList<>(), networks1 = new ArrayList<>();
        networks.add(new Network(2,  "tom"));
        networks.add(new Network(3, "dick"));
        networks.add(new Network(4,  "harry"));
        databaseUtilities.addNetworks(networks);

        assertTrue(databaseUtilities.deleteNetworks(networks));

        networks.remove(2);
        databaseUtilities.addNetworks(networks);
        networks1.add(new Network(4,  "harry"));
        assertFalse(databaseUtilities.deleteNetworks(networks1));
        networks1.clear();
        networks1.add(new Network(1,  "REGISTRATION"));
        assertTrue(databaseUtilities.deleteNetworks(networks1));
    }






}