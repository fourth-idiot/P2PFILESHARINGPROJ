import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;


public class PeerProcess {

    public static void main(String[] args) {
        // Read the current peer id
        int id = Integer.parseInt(args[0]);

        // Object to read the file
        ReadFile readFileObject = new ReadFile();

        // Read Common.cfg
        List<String> commonCfgLines = readFileObject.read(Constants.COMMON_CONFIG_FILE_NAME);
        CommonCfg commonCfg = new CommonCfg();
        commonCfg.parse(commonCfgLines);
        System.out.println(commonCfg);

        // Read PeerInfo.cfg
        List<String> peerInfoLines = readFileObject.read(Constants.PEER_INFO_CONFIG_FILE_NAME);
        PeerInfoCfg peerInfoCfg = new PeerInfoCfg();
        peerInfoCfg.parse(peerInfoLines);
        System.out.println(peerInfoCfg);
        
        // Create peer instance
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        Peer peer = new Peer(id, commonCfg, peerInfoCfg, executorService);
    }
}