package datasource;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DatabaseUtilities {

    private static Connection conn;
    private static ReadPropertiesFile propertiesFile;
    private static DatabaseUtilities databaseUtilities;

    private Pattern aliasPattern = Pattern.compile("\\w{1,255}");

    private static final String DB_NAME = "secure_messenger_relay";
    private static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/?useSSL=false";

    private static final String GET_LOCK = "SELECT GET_LOCK(?, 0)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";
    private static final String CREATE_DB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
    private static final String USE_DB = "USE " + DB_NAME;


private static final String CREATE_ACCOUNT_CONTACT = "CREATE TABLE IF NOT EXISTS accountContact(aid INTEGER, " +
        "cid INTEGER, PRIMARY KEY(aid,cid), FOREIGN KEY(aid) REFERENCES accounts(aid) ON DELETE CASCADE, " +
        "FOREIGN KEY(cid) REFERENCES contacts(cid) ON DELETE CASCADE)";
    private static final String CREATE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS accounts(aid INTEGER PRIMARY KEY, " +
            "username VARCHAR(255) UNIQUE NOT NULL, password TEXT, salt CHAR(84) NOT NULL, iterations INTEGER NOT NULL)";
    private static final String CREATE_CONTACTS = "CREATE TABLE IF NOT EXISTS contacts(cid INTEGER PRIMARY KEY, " +
            "alias VARCHAR(255) NOT NULL)";
    private static final String CREATE_PORTS = "CREATE TABLE IF NOT EXISTS ports(pid INTEGER PRIMARY KEY, " +
            "port INTEGER UNIQUE NOT NULL)";
    private static final String CREATE_NETWORK_PORTS = "CREATE TABLE IF NOT EXISTS networkPorts(nid INTEGER, pid INTEGER, " +
            "PRIMARY KEY(nid, pid), FOREIGN KEY(nid) REFERENCES networks(nid) ON DELETE CASCADE, FOREIGN KEY(pid) " +
            "REFERENCES ports(pid))";
    private static final String CREATE_NETWORKS = "CREATE TABLE IF NOT EXISTS networks(nid INTEGER PRIMARY KEY, " +
            "network_alias VARCHAR(255) UNIQUE NOT NULL)";
    private static final String CREATE_CHATROOMS = "CREATE TABLE IF NOT EXISTS chatrooms(rid INTEGER PRIMARY KEY, " +
            "room_alias VARCHAR(255) NOT NULL)";
    private static final String CREATE_NETWORK_CONTACTS = "CREATE TABLE IF NOT EXISTS networkContacts(nid INTEGER, " +
            "cid INTEGER, PRIMARY KEY(nid,cid), FOREIGN KEY(nid) REFERENCES networks(nid) ON DELETE CASCADE, " +
            "FOREIGN KEY(cid) REFERENCES contacts(cid) ON DELETE CASCADE)";
    private static final String CREATE_CHATROOM_CONTACTS = "CREATE TABLE IF NOT EXISTS chatroomContacts( rid INTEGER, " +
            "cid INTEGER, PRIMARY KEY(rid,cid), FOREIGN KEY(rid) REFERENCES chatrooms(rid) ON DELETE CASCADE, " +
            "FOREIGN KEY(cid) REFERENCES contacts(cid) ON DELETE CASCADE)";

    private static final String UPDATE_NETWORK_PORTS = "UPDATE ports SET port = ?  WHERE pid = ?";
    private static final String UPDATE_NETWORK_ALIASES = "UPDATE networks SET network_alias = ? WHERE nid = ?";
    private static final String SELECT_ALL_NETWORKS = "SELECT n.nid, p.port, n.network_alias FROM networks n INNER JOIN " +
            "networkPorts np ON n.nid = np.nid INNER JOIN ports p ON np.pid = p.pid";
    private static final String INSERT_NETWORKS = "INSERT INTO networks(nid, network_alias) VALUES(?,?)";
    private static final String SELECT_NETWORKS = "SELECT network_alias FROM networks WHERE nid = ?";
    private static final String DELETE_NETWORKS = "DELETE FROM networks WHERE nid = ?";
    private static final String DELETE_CONTACTS = "DELETE contacts FROM contacts INNER JOIN networkContacts nc " +
            "ON nc.cid = contacts.cid INNER JOIN networks n ON n.nid = nc.nid WHERE n.nid = ?";
    private static final String DELETE_CHATROOMS = "DELETE chatrooms FROM chatrooms INNER JOIN chatroomContacts cc ON " +
            "chatrooms.rid = cc.rid INNER JOIN contacts c ON cc.cid = c.cid INNER JOIN networkContacts nc " +
            "ON c.cid = nc.cid WHERE nc.nid = ?";
    private static final String DELETE_ACCOUNTS = "DELETE accounts FROM accounts INNER JOIN accountContact ac " +
            "ON accounts.aid = ac.aid INNER JOIN contacts c ON ac.cid = c.cid INNER JOIN networkContacts nc ON " +
            "c.cid = nc.cid WHERE nc.nid = ?";
    private static final String INSERT_NETWORK_PORTS = "INSERT INTO networkPorts(nid, pid) VALUES(?,?)";
    private static final String INSERT_IGNORE_PORTS = "INSERT IGNORE INTO ports(pid, port) VALUES(?,?)";
    private static final String INSERT_IGNORE_NETWORKS = "INSERT IGNORE INTO networks(nid, network_alias) VALUES(?,?)";
    private static final String INSERT_IGNORE_NETWORK_PORTS = "INSERT IGNORE INTO networkPorts(nid, pid) VALUES(?,?)";

    private static final String RETRIEVE_MAX_NID = "SELECT COALESCE(MAX(nid), 0) FROM networks";

    private static PreparedStatement queryUpdateNetworkPorts;
    private static PreparedStatement queryUpdateNetworkAliases;
    private static PreparedStatement querySelectAllNetworks;
    private static PreparedStatement queryInsertNetworks;
    private static PreparedStatement querySelectNetworks;
    private static PreparedStatement queryDeleteNetworks;
    private static PreparedStatement queryDeleteContacts;
    private static PreparedStatement queryDeleteChatrooms;
    private static PreparedStatement queryDeleteAccounts;
    private static PreparedStatement queryInsertNetworkPorts;
    private static PreparedStatement queryInsertIgnorePorts;
    private static PreparedStatement queryInsertIgnoreNetworks;
    private static PreparedStatement queryInsertIgnoreNetworkPorts;
    private static PreparedStatement queryGetLock;
    private static PreparedStatement queryReleaseLock;

    private static PreparedStatement queryRetrieveMaxNid;

    private static int networkCounter;

    private DatabaseUtilities(String username, String password) throws SQLException {
        openConnection(username, password);
        setupPreparedStatements();
        getLock();
        setupCounters();
        seedDatabase();
    }


    // NOT SURE WHETHER TO HANDLE EXCEPTION HERE OR NOT
    public static void setDatabaseUtilities(String username, String password) throws SQLException, IOException {
        if (databaseUtilities == null)
            propertiesFile = ReadPropertiesFile.getInstance();
            databaseUtilities = new DatabaseUtilities(username, password);


    }

    public static DatabaseUtilities getInstance() throws SQLException{
        if(databaseUtilities != null)
            return databaseUtilities;
        else
            System.out.println("failure: cannot make changes while the relay is active");
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
        querySelectAllNetworks = conn.prepareStatement(SELECT_ALL_NETWORKS);
        queryInsertNetworks = conn.prepareStatement(INSERT_NETWORKS);
        querySelectNetworks = conn.prepareStatement(SELECT_NETWORKS);
        queryDeleteNetworks = conn.prepareStatement(DELETE_NETWORKS);
        queryDeleteContacts = conn.prepareStatement(DELETE_CONTACTS);
        queryDeleteChatrooms = conn.prepareStatement(DELETE_CHATROOMS);
        queryDeleteAccounts = conn.prepareStatement(DELETE_ACCOUNTS);
        queryInsertNetworkPorts = conn.prepareStatement(INSERT_NETWORK_PORTS);
        queryInsertIgnorePorts = conn.prepareStatement(INSERT_IGNORE_PORTS);
        queryInsertIgnoreNetworks = conn.prepareStatement(INSERT_IGNORE_NETWORKS);
        queryInsertIgnoreNetworkPorts = conn.prepareStatement(INSERT_IGNORE_NETWORK_PORTS);
        queryGetLock = conn.prepareStatement(GET_LOCK);
        queryReleaseLock = conn.prepareStatement(RELEASE_LOCK);

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

    private void getLock() throws SQLException{

    queryGetLock.clearParameters();
    queryGetLock.setString(1, propertiesFile.getDbLock());
    ResultSet resultSet = queryGetLock.executeQuery();
    resultSet.next();
    if(resultSet.getInt(1) == 0)
        throw  new SQLException();
    }

    private void releaseLock() throws SQLException{

       queryReleaseLock.clearParameters();
       queryReleaseLock.setString(1, propertiesFile.getDbLock());
       queryReleaseLock.execute();
    }

    /**
     * sets up tables which do not already exist
     */
    private void setupDatabase() throws SQLException {

        Statement statement = conn.createStatement();

        statement.execute(CREATE_DB);
        statement.execute(USE_DB);

        statement.execute(CREATE_ACCOUNTS);
        statement.execute(CREATE_CONTACTS);
        statement.execute(CREATE_ACCOUNT_CONTACT);
        statement.execute(CREATE_PORTS);
        statement.execute(CREATE_NETWORKS);
        statement.execute(CREATE_NETWORK_PORTS);
        statement.execute(CREATE_CHATROOMS);
        statement.execute(CREATE_NETWORK_CONTACTS);
        statement.execute(CREATE_CHATROOM_CONTACTS);

        statement.executeBatch();
        statement.close();
    }


    /**
     * Closes the connection when the application closes
     */
    public void closeConnection() {

        try {
            releaseLock();

            if(queryUpdateNetworkPorts != null){
                queryUpdateNetworkPorts.close();
            }
            if(queryUpdateNetworkAliases != null){
                queryUpdateNetworkAliases.close();
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
            if(queryRetrieveMaxNid != null){
                queryRetrieveMaxNid.close();
            }
            if(queryDeleteContacts != null){
                queryDeleteContacts.close();
            }
            if(queryDeleteChatrooms != null){
                queryDeleteChatrooms.close();
            }
            if(queryDeleteAccounts != null){
                queryDeleteAccounts.close();
            }
            if(queryInsertNetworkPorts != null){
                queryInsertNetworkPorts.close();
            }
            if(queryInsertIgnorePorts != null){
                queryInsertIgnorePorts.close();
            }
            if(queryInsertIgnoreNetworks != null){
                queryInsertIgnoreNetworks.close();
            }
            if(queryInsertIgnoreNetworkPorts != null){
                queryInsertIgnoreNetworkPorts.close();
            }
            if(queryGetLock != null){
                queryGetLock.close();
            }
            if(queryReleaseLock != null){
                queryReleaseLock.close();
            }
            if (conn != null) {
                conn.close();
            }
            databaseUtilities = null;

        } catch (SQLException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }

    private void seedDatabase() throws SQLException{

        queryInsertIgnoreNetworks.clearParameters();
        queryInsertIgnoreNetworks.setInt(1, propertiesFile.getRegDefaultNid());
        queryInsertIgnoreNetworks.setString(2,propertiesFile.getRegDefaultAlias());
        queryInsertIgnoreNetworks.executeUpdate();

        queryInsertIgnorePorts.clearParameters();
        queryInsertIgnorePorts.setInt(1, propertiesFile.getRegDefaultPid());
        queryInsertIgnorePorts.setInt(2,propertiesFile.getRegDefaultPort());
        queryInsertIgnorePorts.executeUpdate();

        queryInsertIgnoreNetworkPorts.clearParameters();
        queryInsertIgnoreNetworkPorts.setInt(1, propertiesFile.getRegDefaultNid());
        queryInsertIgnoreNetworkPorts.setInt(2, propertiesFile.getRegDefaultPid());
        queryInsertIgnoreNetworkPorts.executeUpdate();

        queryInsertIgnorePorts.clearParameters();
        queryInsertIgnorePorts.setInt(1, propertiesFile.getNetDefaultPid());
        queryInsertIgnorePorts.setInt(2, propertiesFile.getNetDefaultPort());
        queryInsertIgnorePorts.executeUpdate();
        networkCounter++;
    }

    /**
     * Update the specified network ports
     * @param isReg is registration
     * @return whether the method succeeded or not
     */
    public boolean updateNetworkPorts(boolean isReg, int port){
        int pid, count;

        if (port >= 1024 && port <= 65535) {
            if (isReg)
                pid = propertiesFile.getRegDefaultPid();
            else
                pid = propertiesFile.getNetDefaultPid();

            try {
                queryUpdateNetworkPorts.clearParameters();
                queryUpdateNetworkPorts.setInt(1, port);
                queryUpdateNetworkPorts.setInt(2, pid);
                count = queryUpdateNetworkPorts.executeUpdate();
                if(count == 0)
                    throw new SQLException();
                return true;
            } catch (SQLException e) {}
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
                    if (aliasPattern.matcher(network.getNetworkAlias()).matches() && network.getNid() != propertiesFile.getRegDefaultNid()) {

                        queryUpdateNetworkAliases.setString(1, network.getNetworkAlias());
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


    public List<Network> getAllNetworks() throws SQLException {

        List<Network> networks = new ArrayList<>();
        ResultSet resultSet = querySelectAllNetworks.executeQuery();
        if (resultSet.next()) {
            do {
                networks.add(new Network(resultSet.getInt(1), new Port(resultSet.getInt(2)),
                        resultSet.getString(3)));
            } while (resultSet.next());
        }
        return networks;
    }

    public boolean addNetworks(List<Network> networks) {

        try {
            int currentNetCount = networkCounter, tempNetCount;
            try {
                conn.setAutoCommit(false);
                queryInsertNetworks.clearBatch();
                queryInsertNetworkPorts.clearBatch();

                for (Network network : networks) {
                    if (aliasPattern.matcher(network.getNetworkAlias()).matches()) {
                        tempNetCount = networkCounter++;
                        queryInsertNetworks.setInt(1, tempNetCount);
                        queryInsertNetworks.setString(2, network.getNetworkAlias());
                        queryInsertNetworks.addBatch();

                        queryInsertNetworkPorts.setInt(1, tempNetCount);
                        queryInsertNetworkPorts.setInt(2, propertiesFile.getNetDefaultPid());
                        queryInsertNetworkPorts.addBatch();
                    } else {
                        throw new SQLException("Format incorrect");
                    }
                }
                if(Arrays.stream(queryInsertNetworks.executeBatch()).anyMatch(x -> x == 0)) {
                    throw new SQLException("Format incorrect");
                }
                queryInsertNetworkPorts.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                networkCounter = currentNetCount;
                System.out.println("failed to add networks" + e.getMessage());
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }catch (SQLException e){}
        return false;
    }


    public boolean deleteNetworks(List<Network> networks){

        try {
            try {
                conn.setAutoCommit(false);
                queryDeleteNetworks.clearBatch();

                deleteChatrooms(networks);
                deleteAccounts(networks);
                deleteContacts(networks);
                // DELETE ACCOUNTS, ACCOUNTCONTACT, CHATROOMS, CHATROOMCONTACTS, NETWORKPORTS
                // INSERT ABOVE RECORDS TO TEST ALL DELETION

                for (Network network : networks) {
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


    private void deleteContacts(List<Network> networks){
        try {
            queryDeleteContacts.clearBatch();
            for (Network network : networks) {
                queryDeleteContacts.setInt(1, network.getNid());
                queryDeleteContacts.addBatch();
            }
            queryDeleteContacts.executeBatch();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void deleteChatrooms(List<Network> networks){
        try {
            queryDeleteChatrooms.clearBatch();
            for (Network network : networks) {
                queryDeleteChatrooms.setInt(1, network.getNid());
                queryDeleteChatrooms.addBatch();
            }
            queryDeleteChatrooms.executeBatch();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void deleteAccounts(List<Network> networks){
        try {
            queryDeleteAccounts.clearBatch();
            for (Network network : networks) {
                queryDeleteAccounts.setInt(1, network.getNid());
                queryDeleteAccounts.addBatch();
            }
            queryDeleteAccounts.executeBatch();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    // ------------------- TEMPORARY METHODS FOR TESTING PURPOSES ----------------------

    public boolean tempMethod() {

        try {
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM networkPorts");
            statement.execute("DELETE FROM ports");
            statement.execute("DELETE FROM accountContact");
            statement.execute("DELETE FROM accounts");
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

    public List<Network> getNetworks(List<Network> networks) throws SQLException{

        for(Network network : networks){
            querySelectNetworks.clearParameters();
            querySelectNetworks.setInt(1,network.getNid());
            ResultSet resultSet = querySelectNetworks.executeQuery();
            if(resultSet.next()){
                network.setNetworkAlias(resultSet.getString(1));
            } else {
                throw new SQLException();
            }
        }
        return networks;
    }

    public boolean insertForDelete() {

        try {
            Statement statement = conn.createStatement();
            statement.execute("INSERT INTO contacts(cid, alias) VALUES(1, 'james')");
            statement.execute("INSERT INTO chatrooms(rid, room_alias) VALUES(1, 'jamesroom')");
            statement.execute("INSERT INTO accounts(aid, username, password, salt, iterations) VALUES(1, 'jj', 'notpassword', 'salty', 12000)");
            statement.execute("INSERT INTO chatroomContacts(rid, cid) VALUES(1, 1)");
            statement.execute("INSERT INTO accountContact(aid, cid) VALUES(1,1)");
            statement.execute("INSERT INTO networkContacts(nid, cid) VALUES(1, 1)");

            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
