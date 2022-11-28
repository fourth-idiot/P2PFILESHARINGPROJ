public class PeerInfo {
    // Individual peer info object
	private int id;
	private String hostName;
    private int port;
    private boolean hasFile;

    public PeerInfo(int id, String hostName, int port, boolean hasFile) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
        this.hasFile = hasFile;
    }

    public int getId() {
        return id;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public boolean getHasFile() {
        return hasFile;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    @Override
    public String toString() {
        return "id: " + id + "\n" +
            "hostname: " + hostName + "\n" +
            "port: " + port + "\n" +
            "hasFile: " + hasFile + "\n";
    }
}