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

    @After
    public void tearDown() {
        String username = "relay-app";
        String password = "relaypass";
        try {
            setupDatabase(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();
            databaseUtilities.tempMethod();
            databaseUtilities.closeConnection();
        }catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e){}
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
            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort(), 2000);
            assertEquals(networks.get(1).getNetwork_alias(), "tom");
            assertEquals(networks.get(2).getPort(), 3000);
            assertEquals(networks.get(2).getNetwork_alias(), "dick");

            databaseUtilities.closeConnection();


        }catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e){
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
        String delete;
        String delete1;
        String delete2;

        try {
            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort(), 2000);
            assertEquals(networks.get(1).getNetwork_alias(), "tom");
            assertEquals(networks.get(2).getPort(), 3000);
            assertEquals(networks.get(2).getNetwork_alias(), "dick");

            delete = Integer.toString(1);
            delete1 = Integer.toString(networks.get(1).getNid());
            delete2 = Integer.toString(networks.get(2).getNid());

            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"DELETE", delete+","+delete1+","+delete2};
            Main.main(args);

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            try {
                networks = databaseUtilities.getAllNetworks();
                networks.remove(0);
                assertTrue(networks.isEmpty());
            }catch (SQLException e){}

            databaseUtilities.closeConnection();

        }catch (SQLException | OperatorCreationException | GeneralSecurityException | IOException e){
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

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(1).getNid());
            nid2 = Integer.toString(networks.get(2).getNid());


            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "TO", "2001,3001", "WITH", "tomboi,dickboi"};
            Main.main(args);

            // check update

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort(), 2001);
            assertEquals(networks.get(1).getNetwork_alias(), "tomboi");
            assertEquals(networks.get(2).getPort(), 3001);
            assertEquals(networks.get(2).getNetwork_alias(), "dickboi");

            databaseUtilities.closeConnection();

            // ??? CHECK THAT UPDATES TO THE ALIAS FOR REG ARE IGNORED

        }catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e){
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

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(1).getNid());
            nid2 = Integer.toString(networks.get(2).getNid());

            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "TO", "2001,3001"};
            Main.main(args);

            // check update

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getPort(), 2001);
            assertEquals(networks.get(2).getPort(), 3001);

            databaseUtilities.closeConnection();

        }catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e){
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

            setupDatabase(username,password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            nid1 = Integer.toString(networks.get(1).getNid());
            nid2 = Integer.toString(networks.get(2).getNid());

            databaseUtilities.closeConnection();

            System.setIn(new ByteArrayInputStream((username +"\r" + password).getBytes()));
            args = new String[]{"UPDATE", nid1+","+nid2, "WITH", "tomboi,dickboi"};
            Main.main(args);

            // check update

            setupDatabase(username, password);
            databaseUtilities = DatabaseUtilities.getInstance();

            networks = databaseUtilities.getAllNetworks();
            assertEquals(networks.get(1).getNetwork_alias(), "tomboi");
            assertEquals(networks.get(2).getNetwork_alias(), "dickboi");

            databaseUtilities.closeConnection();

        }catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e){
            e.getMessage();
            fail();
        }
    }

    private static void setupDatabase(String username, String password)
            throws  SQLException, GeneralSecurityException, OperatorCreationException, IOException
    {
        ReadPropertiesFile propertiesFile = ReadPropertiesFile.getInstance();
        DatabaseUtilities.setDatabaseUtilities(username, password);
        if(!DatabaseUtilities.containsRegister()){
            KeyPair kp = SecurityUtilities.generateKeyPair();
            X509Certificate regCert = SecurityUtilities.makeV1Certificate(kp.getPrivate(), kp.getPublic(), propertiesFile.getReg_default_alias());
            String reg_fingerprint = SecurityUtilities.calculateFingerprint(regCert.getEncoded());
            SecurityUtilities.storePrivateKeyEntry(password, kp.getPrivate(), new X509Certificate[]{regCert}, reg_fingerprint);
            SecurityUtilities.storeCertificate(password, regCert, reg_fingerprint);
            DatabaseUtilities.initRegister(reg_fingerprint);
        }
    }
}