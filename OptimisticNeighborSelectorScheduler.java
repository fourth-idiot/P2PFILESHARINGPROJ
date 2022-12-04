import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

public class OptimisticNeighborSelectorScheduler implements Runnable{

    private final int id;
    private final ExecutorService executorService;
    private Peer peer;
    private Random random;

    public OptimisticNeighborSelectorScheduler(int id, ExecutorService executorService, Peer peer){
        this.id = id;
        this.executorService = executorService;
        this.peer = peer;
        random = new Random();
        
    }

    @Override
    public void run(){

        if (Thread.currentThread().isInterrupted())
            return;

        List<Integer> chokedPeers = new ArrayList<>();
        for (int id : peer.getPeerInfoCfg().getPeers().keySet()){
            if (!peer.getUnchokedNeighborsList().contains(id))
                chokedPeers.add(id);
        }

        int optimisticNeighbor = chokedPeers.get(random.nextInt(chokedPeers.size()));
        peer.getUnchokedNeighborsList().add(optimisticNeighbor);
        peer.setOptimisticNeighbor(optimisticNeighbor);
        EndPoint ep = peer.getPeerEndPoint(optimisticNeighbor);
        ep.sendMessage(Constants.MessageType.UNCHOKE, executorService);

    }
    
}
