package datasource;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DatabaseUtilities {

    private static DatabaseUtilities databaseUtilities;
    private static Connection conn;

    private Pattern aliasPattern = Pattern.compile("\\w{1,255}");
    private static boolean setupComplete = false;

    private static final String DB_NAME = "secure_messenger_relay";
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/?useSSL=false";

    private static final String CREATE_DB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
    private static final String USE_DB = "USE " + DB_NAME;
    private static final String REG_NETWORK_ALIAS = "REGISTRATION";
    private static final int REG_DEFAULT_PORT = 2048;
    private static final int REG_DEFAULT_NID = 1;

    private static final String CREATE_CONTACTS = "CREATE TABLE IF NOT EXISTS contacts(cid CHAR(88) PRIMARY KEY, alias VARCHAR(255) NOT NULL)";
    private static final String CREATE_NETWORKS = "CREATE TABLE IF NOT EXISTS networks(nid INTEGER PRIMARY KEY, fingerprint CHAR(88) UNIQUE NOT NULL," +
            "port INTEGER UNIQUE NOT NULL, network_alias VARCHAR(255) UNIQUE NOT NULL)";
    private static final String CREATE_CHATROOMS = "CREATE TABLE IF NOT EXISTS chatrooms(rid INTEGER PRIMARY KEY, room_alias VARCHAR(255) NOT NULL)";
    private static final String CREATE_NETWORK_CONTACTS = "CREATE TABLE IF NOT EXISTS networkContacts( nid INTEGER, cid CHAR(88), PRIMARY KEY(nid,cid), " +
            "FOREIGN KEY(nid) REFERENCES networks(nid), FOREIGN KEY(cid) REFERENCES contacts(cid))";
    private static final String CREATE_CHATROOM_CONTACTS = "CREATE TABLE IF NOT EXISTS chatroomContacts( rid INTEGER, cid CHAR(88), PRIMARY KEY(rid,cid), " +
            "FOREIGN KEY(rid) REFERENCES chatrooms(rid), FOREIGN KEY(cid) REFERENCES contacts(cid))";
    private static final String CHECK_REGISTER_EXISTS = "SELECT IF(EXISTS(SELECT * FROM networks WHERE nid = "+ REG_DEFAULT_NID +" AND network_alias = '"+REG_NETWORK_ALIAS+"'), 1, 0)";

    private static final String UPDATE_NETWORK_PORTS = "UPDATE networks SET port = ? WHERE nid = ?";
    private static final String UPDATE_NETWORK_ALIASES = "UPDATE networks SET network_alias = ? WHERE nid = ?";
    private static final String UPDATE_NETWORKS = "UPDATE networks SET port = ?, network_alias = ? WHERE nid = ?";
    // ??? MAYBE ALSO PRINT OUT FINGERPRINT
    private static final String SELECT_ALL_NETWORKS = "SELECT * FROM networks";
    private static final String INSERT_NETWORKS = "INSERT INTO networks(nid, fingerprint, port, network_alias) VALUES(?,?,?,?)";
    private static final String SELECT_NETWORKS = "SELECT fingerprint, port, network_alias FROM networks WHERE nid = ?";
    private static final String DELETE_NETWORKS = "DELETE FROM networks WHERE nid = ?";
    private static final String DELETE_NETWORKCONTACTS_NID = "DELETE FROM networkContacts WHERE nid = ?";
    private static final String DELETE_CONTACTS = "DELETE FROM contacts c INNER JOIN networkContacts nc ON nc.cid = c.cid INNER JOIN networks n ON n.nid = nc.nid WHERE n.nid = ?";

    private static final String RETRIEVE_MAX_NID = "SELECT COALESCE(MAX(nid), 0) FROM networks";

    private static PreparedStatement queryUpdateNetworkPorts;
    private static PreparedStatement queryUpdateNetworkAliases;
    private static PreparedStatement queryUpdateNetworks;
    private static PreparedStatement querySelectAllNetworks;
    private static PreparedStatement queryInsertNetworks;
    private static PreparedStatement querySelectNetworks;
    private static PreparedStatement queryDeleteNetworks;
    private static PreparedStatement queryDeleteNetworkContactsNid;
    private static PreparedStatement queryDeleteContacts;

    private static PreparedStatement queryRetrieveMaxNid;

    private static int networkCounter;

    private DatabaseUtilities(String username, String password) throws SQLException {
        openConnection(username, password);
        setupPreparedStatements();
        setupCounters();
    }


    // NOT SURE WHETHER TO HANDLE EXCEPTION HERE OR NOT
    public static void setDatabaseUtilities(String username, String password) throws SQLException {
        if (databaseUtilities == null)
            databaseUtilities = new DatabaseUtilities(username, password);
    }

    public static DatabaseUtilities getInstance() throws SQLException{
        if(setupComplete && databaseUtilities != null)
            return databaseUtilities;
        else
            throw new SQLException();
    }

    private void openConnection(String username, String password) throws SQLException {

        conn = DriverManager.getConnection(CONNECTION_STRING, username, password);
        if (conn != null) {
            setupDatabase();
        }

    }

    /**
     * Setup query prepared statements
     */
    private void setupPreparedStatements() throws SQLException {

        queryUpdateNetworkPorts = conn.prepareStatement(UPDATE_NETWORK_PORTS);
        queryUpdateNetworkAliases = conn.prepareStatement(UPDATE_NETWORK_ALIASES);
        queryUpdateNetworks = conn.prepareStatement(UPDATE_NETWORKS);
        querySelectAllNetworks = conn.prepareStatement(SELECT_ALL_NETWORKS);
        queryInsertNetworks = conn.prepareStatement(INSERT_NETWORKS);
        querySelectNetworks = conn.prepareStatement(SELECT_NETWORKS);
        queryDeleteNetworks = conn.prepareStatement(DELETE_NETWORKS);
        queryDeleteNetworkContactsNid = conn.prepareStatement(DELETE_NETWORKCONTACTS_NID);
        queryDeleteContacts = conn.prepareStatement(DELETE_CONTACTS);

        queryRetrieveMaxNid = conn.prepareStatement(RETRIEVE_MAX_NID);
    }

    /**
     * Setup the counter for the nid so we know the next available one on adding new networks
     */
    private void setupCounters() {

        try {
            ResultSet result;
            result = queryRetrieveMaxNid.executeQuery();
            if (result.next())
                networkCounter = result.getInt(1) + 1;
        } catch (SQLException e) {
            System.out.println("Failed to setup counters: " + e.getMessage());
        }
    }

    /**
     * sets up tables which do not already exist
     */
    private void setupDatabase() throws SQLException {

        Statement statement = conn.createStatement();

        statement.execute(CREATE_DB);
        statement.execute(USE_DB);

        statement.addBatch(CREATE_CONTACTS);
        statement.addBatch(CREATE_NETWORKS);
        statement.addBatch(CREATE_CHATROOMS);
        statement.addBatch(CREATE_NETWORK_CONTACTS);
        statement.addBatch(CREATE_CHATROOM_CONTACTS);

        statement.executeBatch();
    }


    /**
     * Closes the connection when the application closes
     */
    public void closeConnection() {

        try {
            if(queryUpdateNetworkPorts != null){
                queryUpdateNetworkPorts.close();
            }
            if(queryUpdateNetworkAliases != null){
                queryUpdateNetworkAliases.close();
            }
            if (queryUpdateNetworks != null) {
                queryUpdateNetworks.close();
            }
            if (querySelectAllNetworks != null) {
                querySelectAllNetworks.close();
            }
            if (queryInsertNetworks != null) {
                queryInsertNetworks.close();
            }
            if(querySelectNetworks != null){
                querySelectNetworks.close();
            }
            if(queryDeleteNetworks != null){
                queryDeleteNetworks.close();
            }
            if(queryDeleteNetworkContactsNid != null){
                queryDeleteNetworkContactsNid.close();
            }
            if (conn != null) {
                conn.close();
            }
            databaseUtilities = null;
            setupComplete = false;

        } catch (SQLException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }


    /**
     * checks if the register network record exists at nid 1 in the database and sets the
     * setupReady variable accordingly
     * @return whether or not the record is set as boolean
     * @throws SQLException
     */
    public static boolean containsRegister() throws SQLException{

        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(CHECK_REGISTER_EXISTS);
        resultSet.next();
        if(resultSet.getInt(1) == 1){
            setupComplete = true;
            return true;
        }
        return false;
    }

    /**
     * This methods sets the register record if it is not set. This method is to be called to set it prior to any
     * other method and to enforce this subsequent to the containsRegister method returning false. The singleton
     * object will not be acessible till setupComplete is true
     * @param fingerprint the fingerprint of the newly created register certificate
     * @throws SQLException
     */
    public static void initRegister(String fingerprint) throws SQLException{

        queryInsertNetworks.clearParameters();
        queryInsertNetworks.setInt(1, networkCounter++);
        queryInsertNetworks.setString(2, fingerprint);
        queryInsertNetworks.setInt(3, REG_DEFAULT_PORT);
        queryInsertNetworks.setString(4, REG_NETWORK_ALIAS);
        int count = queryInsertNetworks.executeUpdate();
        if(count == 0)
            throw new SQLException();
        setupComplete = true;
    }

    /**
     * Update the specified network ports
     * @param networks networks to update
     * @return whether the method succeeded or not
     */
    public boolean updateNetworkPorts(List<Network> networks){
        try {
            try {
                conn.setAutoCommit(false);
                queryUpdateNetworkPorts.clearBatch();

                for (Network network : networks) {
                    if (network.getPort() >= 1024 && network.getPort() <= 65535) {

                        queryUpdateNetworkPorts.setInt(1, network.getPort());
                        queryUpdateNetworkPorts.setInt(2, network.getNid());
                        queryUpdateNetworkPorts.addBatch();

                    } else {
                        throw new SQLException("Format incorrect");
                    }
                }

                if (Arrays.stream(queryUpdateNetworkPorts.executeBatch()).anyMatch(x -> x == 0))
                    throw new SQLException("update failed");
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
        }

        return false;
    }

    /**
     * Update the specified network aliases
     * @param networks the networks to update
     * @return whether the method succeeded or not
     */
    public boolean updateNetworkAliases(List<Network> networks){
        try {
            try {
                conn.setAutoCommit(false);
                queryUpdateNetworkAliases.clearBatch();


                for (Network network : networks) {
                    if (aliasPattern.matcher(network.getNetwork_alias()).matches() && network.getNid() != REG_DEFAULT_NID
                            && !network.getNetwork_alias().equals(REG_NETWORK_ALIAS)) {

                        queryUpdateNetworkAliases.setString(1, network.getNetwork_alias());
                        queryUpdateNetworkAliases.setInt(2, network.getNid());
                        queryUpdateNetworkAliases.addBatch();

                    } else {
                        throw new SQLException();
                    }
                }

                if (Arrays.stream(queryUpdateNetworkAliases.executeBatch()).anyMatch(x -> x == 0))
                    throw new SQLException();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
        }
        return false;
    }


    // MUST BE UPDATED TO ALLOW SELECTION OF JUST PORTS OR ALIASES
    public boolean updateNetworks(List<Network> networks) {

        try {
            try {
                conn.setAutoCommit(false);
                queryUpdateNetworks.clearBatch();

                for (Network network : networks) {
                    if (network.getPort() >= 1024 && network.getPort() <= 65535 && aliasPattern.matcher(network.getNetwork_alias()).matches()
                            && network.getNid() != REG_DEFAULT_NID && !network.getNetwork_alias().equals(REG_NETWORK_ALIAS)) {

                        queryUpdateNetworks.setInt(1, network.getPort());
                        queryUpdateNetworks.setString(2, network.getNetwork_alias());
                        queryUpdateNetworks.setInt(3, network.getNid());
                        queryUpdateNetworks.addBatch();

                    } else {
                        throw new SQLException();
                    }
                }

                if (Arrays.stream(queryUpdateNetworks.executeBatch()).anyMatch(x -> x == 0))
                    throw new SQLException();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
        }
        return false;
    }

    public List<Network> getAllNetworks() throws SQLException {

        List<Network> networks = new ArrayList<>();
        ResultSet resultSet = querySelectAllNetworks.executeQuery();
        if (resultSet.next()) {
            do {
                networks.add(new Network(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3),
                        resultSet.getString(4)));
            } while(resultSet.next());
            return networks;
        } else {
            throw new SQLException("no networks exist");
        }
    }

    public boolean addNetworks(List<Network> networks) {

        try {
            try {
                conn.setAutoCommit(false);
                queryInsertNetworks.clearBatch();

                for (Network network : networks) {
                    if (network.getPort() >= 1024 && network.getPort() <= 65535 && aliasPattern.matcher(network.getNetwork_alias()).matches()) {
                        queryInsertNetworks.setInt(1, networkCounter++);
                        queryInsertNetworks.setString(2, network.getFingerprint());
                        queryInsertNetworks.setInt(3, network.getPort());
                        queryInsertNetworks.setString(4, network.getNetwork_alias());
                        queryInsertNetworks.addBatch();

                    } else {
                        throw new SQLException("Format incorrect");
                    }
                }
                if(Arrays.stream(queryInsertNetworks.executeBatch()).anyMatch(x -> x == 0))
                    throw new SQLException("Format incorrect");

                conn.commit();
                return true;

            } catch (SQLException e) {
                System.out.println("failed to add networks" + e.getMessage());
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }catch (SQLException e){}
        return false;
    }

    public List<Network> getNetworks(List<Network> networks) throws SQLException{

        for(Network network : networks){
            querySelectNetworks.clearParameters();
            querySelectNetworks.setInt(1,network.getNid());
            ResultSet resultSet = querySelectNetworks.executeQuery();
            if(resultSet.next()){
                network.setFingerprint(resultSet.getString(1));
                network.setPort(resultSet.getInt(2));
                network.setNetwork_alias(resultSet.getString(3));
            } else {
                // ??? WOULD AN EMPTY ARRAY NOT BE BETTER !!! DON'T THINK SO BECAUSE WE WANT DO DELETE ALL OR NONE
                throw new SQLException();
            }
        }
        return networks;
    }


    public boolean deleteNetworks(List<Network> networks){

        try {
            try {
                conn.setAutoCommit(false);
                queryDeleteNetworks.clearBatch();

                deleteContacts(networks);
                deleteNetworkContacts(networks);

                for (Network network : networks) {
                    if(network.getNid() == REG_DEFAULT_NID)
                        throw new SQLException();
                    queryDeleteNetworks.setInt(1, network.getNid());
                    queryDeleteNetworks.addBatch();
                }
                if (Arrays.stream(queryDeleteNetworks.executeBatch()).anyMatch(x -> x == 0))
                    throw new SQLException("update failed");
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
        }
        return false;
    }

    /**
     * A private helper to deleteNetworks this method deletes network contacts
     * @param networks the networks to delete contacts for
     * @throws SQLException
     */
    private void deleteNetworkContacts(List<Network> networks){
        try {
            queryDeleteNetworkContactsNid.clearBatch();
            for (Network network : networks) {
                queryDeleteNetworkContactsNid.setInt(1, network.getNid());
                queryDeleteNetworkContactsNid.addBatch();
            }
            queryDeleteNetworkContactsNid.executeBatch();
        }catch (SQLException e){}
     }


    private void deleteContacts(List<Network> networks){
        try {
            queryDeleteContacts.clearBatch();
            for (Network network : networks) {
                queryDeleteContacts.setInt(1, network.getNid());
                queryDeleteContacts.addBatch();
            }
            queryDeleteContacts.executeBatch();
        }catch (SQLException e){}
    }

    // A TEMPORARY METHOD FOR TESTING PURPOSES
    public boolean tempMethod() {

        try {
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM networkContacts");
            statement.execute("DELETE FROM chatroomContacts");
            statement.execute("DELETE FROM networks");
            statement.execute("DELETE FROM contacts");
            statement.execute("DELETE FROM chatrooms");

            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
