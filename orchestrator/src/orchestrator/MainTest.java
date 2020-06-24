package orchestrator;

import datasource.DatabaseUtilities;
import datasource.Network;
import datasource.ReadPropertiesFile;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.After;
import security.SecurityUtilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;


public class MainTest {

    private DatabaseUtilities databaseUtilities;



    @org.junit.Test
    public void mainDisplay(){
        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "tom,dick,harry"};
        Main.main(args);

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
        System.out.println();
        args = new String[]{"DISPLAY"};
        Main.main(args);

    }

    @org.junit.Test
    public void mainAdd(){

        String username = "relay-app";
        String password = "relaypass";

        System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));

        String[] args;
        args = new String[]{"ADD", "tom,dick,harry"};
        Main.main(args);

        List<Network> networks;

        try {
            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort().getTLSPort(), 2049);
            assertEquals(networks.get(1).getNetworkAlias(), "tom");
            assertEquals(networks.get(2).getPort().getTLSPort(), 2049);
            assertEquals(networks.get(2).getNetworkAlias(), "dick");

            databaseUtilities.tempMethod();
            databaseUtilities.closeConnection();
        }catch (SQLException | IOException e){
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
        args = new String[]{"ADD", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String delete;
        String delete1;
        String delete2;

        try {
            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            databaseUtilities.insertForDelete();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort().getTLSPort(), 2049);
            assertEquals(networks.get(1).getNetworkAlias(), "tom");
            assertEquals(networks.get(2).getPort().getTLSPort(), 2049);
            assertEquals(networks.get(2).getNetworkAlias(), "dick");

            delete = Integer.toString(1);
            delete1 = Integer.toString(networks.get(1).getNid());
            delete2 = Integer.toString(networks.get(2).getNid());

            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", delete+","+delete1+","+delete2};
            Main.main(args);

            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            try {
                networks = databaseUtilities.getAllNetworks();
                networks.remove(0);
                assertTrue(networks.isEmpty());

                databaseUtilities.tempMethod();
                databaseUtilities.closeConnection();
            }catch (SQLException e){}


        }catch (SQLException | IOException e){
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
        args = new String[]{"ADD", "tom,dick"};
        Main.main(args);

        List<Network> networks;
//        String nid1;
//        String nid2;

        try {

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", "R", "TO", "5001"};
            Main.main(args);

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", "O", "TO", "5002"};
            Main.main(args);
            // check update

            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(0).getPort().getTLSPort(), 5001);
            assertEquals(networks.get(2).getPort().getTLSPort(), 5002);

            databaseUtilities.tempMethod();
            databaseUtilities.closeConnection();

        }catch (SQLException | IOException e){
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
        args = new String[]{"ADD", "tom,dick"};
        Main.main(args);

        List<Network> networks;
        String nid1;
        String nid2;

        try {
            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(1).getNid());
            nid2 = Integer.toString(networks.get(2).getNid());

            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "WITH", "tomboi,dickboi"};
            Main.main(args);

            // check update
            DatabaseUtilities.setDatabaseUtilities(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getNetworkAlias(), "tomboi");
            assertEquals(networks.get(2).getNetworkAlias(), "dickboi");

            databaseUtilities.tempMethod();
            databaseUtilities.closeConnection();
        }catch (SQLException  | IOException e){
            e.getMessage();
            fail();
        }
    }



}