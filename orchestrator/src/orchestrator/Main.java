package orchestrator;

import datasource.DatabaseUtilities;
import datasource.Network;
import datasource.ReadPropertiesFile;
import org.bouncycastle.operator.OperatorCreationException;
import security.SecurityUtilities;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static boolean success = false;
    private static List<Network> networks;
    private static DatabaseUtilities databaseUtilities;
    private static ReadPropertiesFile propertiesFile;

// CLEAN UP AND POSSIBLY ADD FUNCTIONALITY TO CREATE NEW CERT

    public static void main(String[] args) {

        try {
            if (args[0].equals("--help") || args[0].equals("-h")) {
                printHelp();
                return;
            }

            networks = new ArrayList<>();
            String[] credentials = getCredentials();
            propertiesFile = ReadPropertiesFile.getInstance();
            genCertificate(credentials[0]);
            DatabaseUtilities.setDatabaseUtilities(credentials[0], credentials[1]);
            databaseUtilities = DatabaseUtilities.getInstance();

            if (args[0].equals("UPDATE")) {

                if (args.length == 4 && args[2].equals("TO")) {

                    boolean isReg;
                    int port;
                    if (args[1].equals("R"))
                        isReg = true;
                    else if (args[1].equals("O"))
                        isReg = false;
                    else
                        return;

                    port = Integer.parseInt(args[3]);
                    success = databaseUtilities.updateNetworkPorts(isReg, port);

                } else if (args.length == 4 && args[2].equals("WITH")) {

                    String[] tempNets, tempAliases;

                    tempNets = args[1].split(",");
                    tempAliases = args[3].split(",");

                    for (int i = 0; i < tempNets.length; i++) {
                        int tempNet = Integer.parseInt(tempNets[i]);
                        String tempAlias = tempAliases[i];
                        networks.add(new Network(tempNet, tempAlias));
                    }
                    if (!networks.isEmpty())
                        success = databaseUtilities.updateNetworkAliases(networks);
                }

                printOutcome(success, "update successful..", "update failed, try 'relay --help' for valid syntax or 'relay DISPLAY' for networks");
            } else if (args[0].equals("DISPLAY")) {

                // !!! This is capable of throwing an exception here either on failure or a lack of results should probably handle with a try catch
                networks = databaseUtilities.getAllNetworks();
                printNetworks(networks);


            } else if (args[0].equals("ADD") && args.length == 2) {

                String[] tempAliases;
                tempAliases = args[1].split(",");

                for (int i = 0; i < tempAliases.length; i++) {
                    String tempAlias = tempAliases[i];
                    networks.add(new Network(tempAlias));
                }
                success = databaseUtilities.addNetworks(networks);
                printOutcome(success, "addition successful..", "addition failed, try 'relay --help' for valid syntax");

            } else if (args[0].equals("DELETE") && args.length == 2) {

                String[] tempNets;

                    tempNets = args[1].split(",");
                    networks = Arrays.stream(tempNets).map(Integer::parseInt).map(Network::new).collect(Collectors.toList());

                    if (!networks.isEmpty()) {
                        databaseUtilities.deleteNetworks(networks);
                        success = true;
                        }
                printOutcome(success, "deletion successful..", "deletion failed, try 'relay --help for valid syntax or relay DISPLAY for networks");
            }


            databaseUtilities.closeConnection();

        } catch (SQLException | GeneralSecurityException | IOException | OperatorCreationException e) {
            // WILL HANDLE FAILED DELETE
            System.out.println(e.getMessage());
        }

    }

    private static void printHelp() {

        System.out.println("Usage: relay {COMMAND | --help | -h}");
        System.out.println("              UPDATE { network_list...  WITH alias_list... | { R | O } TO port }");
        System.out.println("                    update a comma separated list of networks to the comma separated list of\n" +
                "                               list of aliases alternatively update R (register network) or O\n" +
                "                               (other networks) with a new port");
        System.out.println("              DISPLAY");
        System.out.println("                    display all networks with their respective ports and aliases");
        System.out.println("              ADD alias_list...");
        System.out.println("                    add with auto incremented network ids a comma separated list of their\n" +
                "                               aliases");
        System.out.println("              DELETE network_list...");
        System.out.println("                    delete a comma separated list of networks");
    }

    private static void printNetworks(List<Network> networks) {

        System.out.println("--------------------RELAY NETWORKS--------------------");
        System.out.println("network id          TLS port            network alias");
        for (Network network : networks)
            System.out.printf("%-20s%-20s%s\n", Integer.toString(network.getNid()), Integer.toString(network.getPort().getTLSPort()),
                    network.getNetworkAlias());
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

    private static void genCertificate(String password)
            throws GeneralSecurityException, OperatorCreationException, IOException {
        if (!SecurityUtilities.keystoreExists(password, propertiesFile.getServerCert())) {
            KeyPair kp = SecurityUtilities.generateKeyPair();
            X509Certificate serverCert = SecurityUtilities.makeV1Certificate(kp.getPrivate(), kp.getPublic(), propertiesFile.getServerCert());
            SecurityUtilities.storePrivateKeyEntry(password, kp.getPrivate(), new X509Certificate[]{serverCert}, propertiesFile.getServerCert());
        }
    }

}
