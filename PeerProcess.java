import java.util.List;

public class PeerProcess {
    public static void main(String[] args) {
        // Read PeerInfo.cfg
        ReadFile readFileObject = new ReadFile();
        List<String> lines = readFileObject.read(Constants.PEER_INFO_CONFIG_FILE_NAME);
        PeersInfo peersInfo = new PeersInfo();
        peersInfo.parse(lines);
        List<PeerInfo> peers = peersInfo.getPeers();
        for(PeerInfo peer: peers) {
            System.out.println(peer);
        }
    }
}