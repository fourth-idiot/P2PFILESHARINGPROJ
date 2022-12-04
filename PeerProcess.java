import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.nio.file.Paths;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PeerProcess {
    private static final Logger LOGGER = LogManager.getLogger(PeerProcess.class);

    private static void createRequiredDirStructure(PeerInfoCfg.PeerInfo peerInfo, String workingDir, String inputFileName) throws Exception {
        String[] createDirectoryStructureCommand = new String[] {"sh", "-c", String.format("mkdir -p %s", Paths.get(workingDir, String.format("peer_%d", peerInfo.getId())).toString())};
        Process createDirectoryStructureProcess = Runtime.getRuntime().exec(createDirectoryStructureCommand);
        createDirectoryStructureProcess.waitFor();
        
        if(peerInfo.getHasFile()) {
            String[] copyInputFileCommand = new String[] {"sh", "-c", String.format("cp %s %s", Paths.get(workingDir, inputFileName).toString(), Paths.get(workingDir, String.format("peer_%d", peerInfo.getId())).toString())};
            Process copyInputFileProcess = Runtime.getRuntime().exec(copyInputFileCommand);
            copyInputFileProcess.waitFor();
        }   
    }

    public static void main(String[] args) {
        LOGGER.info("Inside main method");

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

        // Create folder structure
        try {
            createRequiredDirStructure(peerInfoCfg.getPeer(id), Constants.WORKING_DIR, commonCfg.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create peer instance
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        Peer peer = new Peer(id, commonCfg, peerInfoCfg, executorService);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
        scheduler.scheduleAtFixedRate(new PreferredNeighborsSelectorScheduler(id, executorService, peer), 0L, commonCfg.getUnchokingInterval(), TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new OptimisticNeighborSelectorScheduler(id, executorService, peer), 0L, commonCfg.getOptimisticUnchokingInterval(), TimeUnit.SECONDS);
        
    }
}