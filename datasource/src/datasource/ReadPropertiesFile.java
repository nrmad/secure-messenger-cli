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
    private static final String reg_default_port_key = "reg-default-port";
    private static final String reg_default_alias_key = "reg-default-alias";
    private final String db_lock;
    private final int reg_default_nid;
    private final int reg_default_port;
    private final String reg_default_alias;

    private ReadPropertiesFile() throws IOException {

        ip = new FileInputStream("/etc/secure-messenger-relay/cli.properties");
        properties.load(ip);
        if(properties.containsKey(db_lock_key) &&
           properties.containsKey(reg_default_nid_key) &&
           properties.containsKey(reg_default_port_key) &&
           properties.containsKey(reg_default_alias_key)){
            db_lock = properties.getProperty(db_lock_key);
            reg_default_nid = Integer.parseInt(properties.getProperty(reg_default_nid_key));
            reg_default_port = Integer.parseInt(properties.getProperty(reg_default_port_key));
            reg_default_alias = properties.getProperty(reg_default_alias_key);
        } else{
            throw new IllegalArgumentException();
        }
    }

    public static ReadPropertiesFile getInstance() throws IOException {
        if(readPropertiesFile == null)
            readPropertiesFile = new ReadPropertiesFile();
        return readPropertiesFile;
    }

    public String getDb_lock() {
            return db_lock;
    }

    public int getReg_default_nid() {
        return reg_default_nid;
    }

    public int getReg_default_port() {
        return reg_default_port;
    }

    public String getReg_default_alias() {
        return reg_default_alias;
    }
}
