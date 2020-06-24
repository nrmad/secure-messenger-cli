package datasource;

import java.util.Objects;

public class Network {

    private int nid;

    private Port port;

    private String network_alias;

    public Network(int nid){
        this.nid = nid;
    }

    public Network(String network_alias){
        this.network_alias = network_alias;
    }

    public Network(int nid, String network_alias){
        this.nid = nid;
        this.network_alias = network_alias;
    }

    public Network(int nid, Port port,String network_alias){
        this.nid = nid;
        this.port = port;
        this.network_alias = network_alias;
    }

    public int getNid() {
        return nid;
    }

    public String getNetworkAlias() {
        return network_alias;
    }

    public Port getPort() { return port; }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public void setNetworkAlias(String network_alias) {
        this.network_alias = network_alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network network = (Network) o;
        return nid == network.nid &&
                Objects.equals(network_alias, network.network_alias);
    }

}
