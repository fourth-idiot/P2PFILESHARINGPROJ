import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;


public class PeerProcess {
    public static void main(String[] args) {
        // Read the peer id
        int id = Integer.parseInt(args[0]);

        // Read PeerInfo.cfg
        ReadFile readFileObject = new ReadFile();
        List<String> lines = readFileObject.read(Constants.PEER_INFO_CONFIG_FILE_NAME);
        PeerInfoCfg peerInfoCfg = new PeerInfoCfg();
        peerInfoCfg.parse(lines);
        List<PeerInfo> peersInfo = peerInfoCfg.getPeers();
        System.out.println("Successfully parsed peerInfo.cfg");
        for(PeerInfo peerInfo: peersInfo) {
            System.out.println(peerInfo);
        }

        // Read Common.cfg
        List<String> commonConfigLines = readFileObject.read(Constants.COMMON_CONFIG_FILE_NAME);
        CommonConfig commonConfig = new CommonConfig();
        commonConfig.parse(commonConfigLines);
        System.out.println(commonConfig);



        // Make connections
        // Peer X will try to establish connection with all the peers started before it
        // With this logic, peer 1001 will be in listening mode
        // Peer 1003 will try to make connection with peer 1001 and peer 1002
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        PeerInfo currentPeerInfo = peerInfoCfg.getPeer(id);
        System.out.println("Current peerInfo is as following:");
        System.out.println(currentPeerInfo);
        executorService.execute(new PeerServer(id, currentPeerInfo.getHostName(), currentPeerInfo.getPort(), executorService));
        executorService.execute(new PeerClient(id, peersInfo, executorService));
    }
}