package datasource;

import java.util.Objects;

public class Network {

    private int nid;
    private String fingerprint;
    private int port;
    private String network_alias;



    /**
     * Network constructor used for Networks containing no alias, fingerprint or port
     * @param nid network id
     */
    public Network(int nid){
        this(nid, "", -1, "");
    }

    /**
     * Network constructor used for Networks containing no fingerprint
     * @param nid network id
     * @param port tls port
     * @param network_alias network alias
     */
    public Network(int nid, int port, String network_alias){
        this(nid, "", port, network_alias);
    }


    /**
     * Network constructor used for Networks containing only port and alias (creating new ones)
     * @param port tls port
     * @param network_alias network alias
     */
    public Network(int port, String network_alias){
        this(-1, "", port, network_alias);
    }

    /**
     * Network contructor used for fully defined networks
     * @param nid network id
     * @param fingerprint public key fingerprint
     * @param port tls port
     * @param network_alias network alias
     */
    public Network(int nid, String fingerprint, int port, String network_alias){
        this.nid = nid;
        this.fingerprint = fingerprint;
        this.port = port;
        this.network_alias = network_alias;
    }

    public int getNid() {
        return nid;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public int getPort() {
        return port;
    }

    public String getNetwork_alias() {
        return network_alias;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setNetwork_alias(String network_alias) {
        this.network_alias = network_alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return nid == network.nid &&
                port == network.port &&
                Objects.equals(fingerprint, network.fingerprint) &&
                Objects.equals(network_alias, network.network_alias);
    }

}
