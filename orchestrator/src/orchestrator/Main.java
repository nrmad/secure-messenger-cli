package orchestrator;

import datasource.DatabaseUtilities;
import datasource.Network;
import org.bouncycastle.operator.OperatorCreationException;
import security.SecurityUtilities;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    // ??? COULD MAKE CONFIGURABLE
    private static final String REG_NETWORK_ALIAS = "REGISTRATION";
    private static final int REG_DEFAULT_PORT = 2048;
    private static final int REG_DEFAULT_NID = 1;

    public static void main(String[] args) {
        boolean success = false;
        List<Network> networks = new ArrayList<>();
        DatabaseUtilities databaseUtilities;

        try {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                printHelp();
                return;
            }

              String[] credentials = getCredentials();
              setupDatabase(credentials[0], credentials[1]);
              databaseUtilities = DatabaseUtilities.getInstance();

            if (args[0].equals("UPDATE")) {

                String[] tempNets, tempPorts, tempAliases;

                if ((args.length == 6 && args[2].equals("TO") && args[4].equals("WITH"))) {

                    tempAliases = args[5].split(",");
                    tempNets = args[1].split(",");
                    tempPorts = args[3].split(",");

                    for (int i = 0; i < tempNets.length; i++) {
                        int tempNet = Integer.parseInt(tempNets[i]);
                        int tempPort = Integer.parseInt(tempPorts[i]);
                        String tempAlias = tempAliases[i];
                        networks.add(new Network(tempNet, tempPort, tempAlias));
                    }

                    if (!networks.isEmpty())
                        success = databaseUtilities.updateNetworks(networks);

                } else if (args.length == 4 && args[2].equals("TO")) {

                    tempNets = args[1].split(",");
                    tempPorts = args[3].split(",");

                    for (int i = 0; i < tempNets.length; i++) {
                        int tempNet = Integer.parseInt(tempNets[i]);
                        int tempPort = Integer.parseInt(tempPorts[i]);
                        networks.add(new Network(tempNet, tempPort, ""));
                    }

                    if (!networks.isEmpty())
                        success = databaseUtilities.updateNetworkPorts(networks);

                } else if (args.length == 4 && args[2].equals("WITH")) {

                    tempNets = args[1].split(",");
                    tempAliases = args[3].split(",");

                    for (int i = 0; i < tempNets.length; i++) {
                        int tempNet = Integer.parseInt(tempNets[i]);
                        String tempAlias = tempAliases[i];
                        networks.add(new Network(tempNet, -1, tempAlias));
                    }
                    if (!networks.isEmpty())
                        success = databaseUtilities.updateNetworkAliases(networks);
                }

                printOutcome(success, "update successful..", "update failed, try 'relay --help' for valid syntax or 'relay DISPLAY' for networks");
            } else if (args[0].equals("DISPLAY")) {

                // !!! This is capable of throwing an exception here either on failure or a lack of results should probably handle with a try catch
                networks = databaseUtilities.getAllNetworks();
                printNetworks(networks);


            } else if (args[0].equals("ADD")) {

                String[] tempPorts, tempAliases;

                if (args.length == 4 && args[2].equals("WITH")) {

                    tempPorts = args[1].split(",");
                    tempAliases = args[3].split(",");

                    for (int i = 0; i < tempPorts.length; i++) {
                        int tempPort = Integer.parseInt(tempPorts[i]);
                        String tempAlias = tempAliases[i];
                        networks.add(new Network(tempPort, tempAlias));
                    }

                    if (!networks.isEmpty()) {

                        

                        for (Network network : networks) {
                            KeyPair kp = SecurityUtilities.generateKeyPair();
                            X509Certificate serverCertificate = SecurityUtilities.makeV1Certificate(kp.getPrivate(), kp.getPublic(), network.getNetwork_alias());
                            network.setFingerprint(SecurityUtilities.calculateFingerprint(serverCertificate.getEncoded()));

                            SecurityUtilities.storePrivateKeyEntry(credentials[1], kp.getPrivate(), new X509Certificate[]{serverCertificate}, network.getFingerprint());
                            SecurityUtilities.storeCertificate(credentials[1], serverCertificate, network.getFingerprint());
                        }

                        success = databaseUtilities.addNetworks(networks);
                    }
                }
                printOutcome(success, "addition successful..", "addition failed, try 'relay --help' for valid syntax");


            } else if (args[0].equals("DELETE")) {

                String[] tempNets;

                if (args.length == 2) {

                    tempNets = args[1].split(",");
                    networks = Arrays.stream(tempNets).map(Integer::parseInt).map(Network::new).collect(Collectors.toList());

                    if (!networks.isEmpty()) {

                        networks = databaseUtilities.getNetworks(networks);
                        if (databaseUtilities.deleteNetworks(networks)) {
                            for (Network network : networks) {
                                SecurityUtilities.deleteCertificate(credentials[1], network.getFingerprint());
                                SecurityUtilities.deletePrivateKeyEntry(credentials[1], network.getFingerprint());
                            }
                            success = true;
                        }
                    }
                }
                printOutcome(success, "deletion successful..", "deletion failed, try 'relay --help for valid syntax or relay DISPLAY for networks");
            }

            databaseUtilities.closeConnection();

        } catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e) {
            // WILL HANDLE FAILED DELETE
        }

    }

    private static void printHelp() {

        System.out.println("Usage: relay {COMMAND | --help | -h}");
        System.out.println("              UPDATE network_list...  { TO port_list...  | WITH alias_list... | TO port_list... WITH alias_list }");
        System.out.println("                    update a comma separated list of networks to the comma separated list of\n" +
                "                    ports with a comma separated list of aliases");
        System.out.println("              DISPLAY");
        System.out.println("                    display all networks with their respective ports and aliases");
        System.out.println("              ADD port_list... WITH alias_list...");
        System.out.println("                    add with auto incremented network ids a comma separated list of their\n" +
                "                    ports with a comma separated list of aliases");
        System.out.println("              DELETE network_list...");
        System.out.println("                    delete a comma separated list of networks");
        System.out.println("              START");
        System.out.println("                    start the relay");
    }

    private static void printNetworks(List<Network> networks) {

        System.out.println("--------------------RELAY NETWORKS--------------------");
        System.out.println("network id          TLS port            network alias");
        for (Network network : networks)
            System.out.printf("%-20s%-20s%s\n", Integer.toString(network.getNid()), Integer.toString(network.getPort()),
                    network.getNetwork_alias());
        System.out.println("------------------------------------------------------");
    }

    private static void printOutcome(boolean success, String successMsg, String failMsg) {

        if (success)
            System.out.println(successMsg);
        else
            System.out.println(failMsg);
    }

    private static String[] getCredentials() {

        //Console con = System.console();
        Scanner scanner = new Scanner(System.in);
        String[] credentials = new String[2];

        System.out.print("Enter username: ");
        credentials[0] = scanner.nextLine();
        System.out.print("Enter password: ");
        credentials[1] = scanner.nextLine();
//        password = new String(con.readPassword("Enter password: "));
        return credentials;
    }

    private static void setupDatabase(String username, String password)
            throws  SQLException, GeneralSecurityException, OperatorCreationException, IOException
    {
        DatabaseUtilities.setDatabaseUtilities(username, password);
        if(!DatabaseUtilities.containsRegister(REG_DEFAULT_NID, REG_NETWORK_ALIAS)){
            KeyPair kp = SecurityUtilities.generateKeyPair();
            X509Certificate regCert = SecurityUtilities.makeV1Certificate(kp.getPrivate(), kp.getPublic(), REG_NETWORK_ALIAS);
            String reg_fingerprint = SecurityUtilities.calculateFingerprint(regCert.getEncoded());
            SecurityUtilities.storePrivateKeyEntry(password, kp.getPrivate(), new X509Certificate[]{regCert}, reg_fingerprint);
            SecurityUtilities.storeCertificate(password, regCert, reg_fingerprint);
            DatabaseUtilities.initRegister(reg_fingerprint, REG_DEFAULT_PORT);
        }
    }
}
