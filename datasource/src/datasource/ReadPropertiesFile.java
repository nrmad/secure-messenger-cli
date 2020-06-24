package datasource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ReadPropertiesFile {

    private static ReadPropertiesFile readPropertiesFile;
    private static Properties properties = new Properties();
    private static FileInputStream ip;
    private static final String db_lock_key = "lock-string";
    private static final String reg_default_nid_key = "reg-default-nid";
    private static final String reg_default_alias_key = "reg-default-alias";
    private static final String reg_default_pid_key = "reg-default-pid";
    private static final String reg_default_port_key = "reg-default-port";
    private static final String net_default_pid_key = "net-default-pid";
    private static final String net_default_port_key = "net-default-port";
    private static final String server_cert_key = "server-cert";
    private final String db_lock;
    private final int reg_default_nid;
    private final String reg_default_alias;
    private final int reg_default_pid;
    private final int reg_default_port;
    private final int net_default_pid;
    private final int net_default_port;
    private final String server_cert;

    private ReadPropertiesFile() throws IOException {

        ip = new FileInputStream("/etc/secure-messenger-relay/relay.properties");
        properties.load(ip);
        if(properties.containsKey(db_lock_key) &&
           properties.containsKey(reg_default_nid_key) &&
           properties.containsKey(reg_default_alias_key) &&
           properties.containsKey(reg_default_pid_key) &&
           properties.containsKey(reg_default_port_key) &&
           properties.containsKey(net_default_port_key) &&
           properties.containsKey(net_default_port_key) &&
           properties.containsKey(server_cert_key)){
            db_lock = properties.getProperty(db_lock_key);
            reg_default_nid = Integer.parseInt(properties.getProperty(reg_default_nid_key));
            reg_default_alias = properties.getProperty(reg_default_alias_key);
            reg_default_pid = Integer.parseInt(properties.getProperty(reg_default_pid_key));
            reg_default_port = Integer.parseInt(properties.getProperty(reg_default_port_key));
            net_default_pid = Integer.parseInt(properties.getProperty(net_default_pid_key));
            net_default_port = Integer.parseInt(properties.getProperty(net_default_port_key));
            server_cert = properties.getProperty(server_cert_key);
        } else{
            throw new IllegalArgumentException();
        }
    }

    public static ReadPropertiesFile getInstance() throws IOException {
        if(readPropertiesFile == null)
            readPropertiesFile = new ReadPropertiesFile();
        return readPropertiesFile;
    }

    public String getDbLock() {
            return db_lock;
    }

    public int getRegDefaultNid() {
        return reg_default_nid;
    }

    public String getRegDefaultAlias() {
        return reg_default_alias;
    }

    public int getRegDefaultPid(){ return reg_default_pid; }

    public int getRegDefaultPort() {
        return reg_default_port;
    }

    public int getNetDefaultPid(){ return net_default_pid; }

    public int getNetDefaultPort() { return net_default_port; }

    public String getServerCert(){ return server_cert; }
}
