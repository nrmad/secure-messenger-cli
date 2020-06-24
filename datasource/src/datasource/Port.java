package datasource;

public class Port {
    private int pid;
    private int port;

    public Port(int pid, int port) {
        this.pid = pid;
        this.port = port;
    }

    public Port(int port) {
        this.port = port;
    }

    public int getPid() {
        return pid;
    }

    public int getTLSPort() {
        return port;
    }
}
