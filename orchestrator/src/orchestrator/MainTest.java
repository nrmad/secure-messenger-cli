package orchestrator;

import datasource.DatabaseUtilities;
import datasource.Network;
import org.junit.After;
import security.SecurityUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;


public class MainTest {

    private DatabaseUtilities databaseUtilities;

    @After
    public void tearDown() {

        databaseUtilities.tempMethod();

    }

    @org.junit.Test
    public void mainAdd(){

        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "2000,3000", "WITH", "tom,dick"};
        Main.main(args);

        List<Network> networks;

        try {
            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getPort(), 2000);
            assertEquals(networks.get(0).getNetwork_alias(), "tom");
            assertEquals(networks.get(1).getPort(), 3000);
            assertEquals(networks.get(1).getNetwork_alias(), "dick");

            SecurityUtilities.deletePrivateKeyEntry(password ,networks.get(0).getFingerprint());
            SecurityUtilities.deleteCertificate(password, networks.get(0).getFingerprint());

            SecurityUtilities.deletePrivateKeyEntry(password ,networks.get(1).getFingerprint());
            SecurityUtilities.deleteCertificate(password, networks.get(1).getFingerprint());

        }catch (SQLException | GeneralSecurityException | IOException e){
            e.getMessage();
            fail();
        }
    }

    @org.junit.Test
    public void mainDelete(){


        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "2000,3000", "WITH", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String delete1;
        String delete2;

        try {
            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getPort(), 2000);
            assertEquals(networks.get(0).getNetwork_alias(), "tom");
            assertEquals(networks.get(1).getPort(), 3000);
            assertEquals(networks.get(1).getNetwork_alias(), "dick");

            delete1 = Integer.toString(networks.get(0).getNid());
            delete2 = Integer.toString(networks.get(1).getNid());



            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", delete1+","+delete2};
            Main.main(args);

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            try {
                networks = databaseUtilities.getAllNetworks();
            }catch (SQLException e){
                assertTrue(true);
            }

        }catch (SQLException e){
            e.getMessage();
            fail();
        }
    }


    @org.junit.Test
    public void mainUpdate() {

        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "2000,3000", "WITH", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String nid1;
        String nid2;

        try {

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(0).getNid());
            nid2 = Integer.toString(networks.get(1).getNid());


            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "TO", "2001,3001", "WITH", "tomboi,dickboi"};
            Main.main(args);

            // check update

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getPort(), 2001);
            assertEquals(networks.get(0).getNetwork_alias(), "tomboi");
            assertEquals(networks.get(1).getPort(), 3001);
            assertEquals(networks.get(1).getNetwork_alias(), "dickboi");

            // clean up

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", nid1+","+nid2};
            Main.main(args);

        }catch (SQLException e){
            e.getMessage();
            fail();
        }
    }

    @org.junit.Test
    public void mainPortUpdate(){

        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "2000,3000", "WITH", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String nid1;
        String nid2;

        try {

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(0).getNid());
            nid2 = Integer.toString(networks.get(1).getNid());


            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "TO", "2001,3001"};
            Main.main(args);

            // check update

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getPort(), 2001);
            assertEquals(networks.get(1).getPort(), 3001);

            // clean up

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", nid1+","+nid2};
            Main.main(args);

        }catch (SQLException e){
            e.getMessage();
            fail();
        }
    }

    @org.junit.Test
    public void mainAliasUpdate(){

        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "2000,3000", "WITH", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String nid1;
        String nid2;

        try {

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(0).getNid());
            nid2 = Integer.toString(networks.get(1).getNid());


            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "WITH", "tomboi,dickboi"};
            Main.main(args);

            // check update

            DatabaseUtilities.setDatabaseUtilities(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getNetwork_alias(), "tomboi");
            assertEquals(networks.get(1).getNetwork_alias(), "dickboi");

            // clean up

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", nid1+","+nid2};
            Main.main(args);

        }catch (SQLException e){
            e.getMessage();
            fail();
        }
    }
}