import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class PeersInfo {
	// All peers info object
    private Map<Integer, PeerInfo> peers = new LinkedHashMap<>();

    public void parse(List<String> lines) {
		for(String line:lines) {
			// Parse individual peer info
			String[] lineSplit = line.split(" ");
			int id = Integer.parseInt(lineSplit[0]);
			String hostName = lineSplit[1];
			int port = Integer.parseInt(lineSplit[2]);
			boolean hasFile = Integer.parseInt(lineSplit[3]) == 1;
			
			// Create an instance of peerInfo and add to the peers
			this.peers.put(id, new PeerInfo(id, hostName, port, hasFile));
		}
	}

    public List<PeerInfo> getPeers() {
        return new ArrayList<>(peers.values());
    }
}