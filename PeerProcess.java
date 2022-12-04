import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeerProcess {
    private static final Logger LOGGER = LogManager.getLogger(PeerProcess.class);

    public static void main(String[] args) {
        
        LOGGER.info("Inside main method");
        // System.setProperty("logFilePath", "path to the log folder");
        String myString = System.getProperty("logFilePath");
        System.out.println(myString);

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

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        scheduler.scheduleAtFixedRate(new PreferredNeighborsSelectorScheduler(id, executorService, peer), 0L, commonCfg.getUnchokingInterval(), TimeUnit.SECONDS);
        
    }
}