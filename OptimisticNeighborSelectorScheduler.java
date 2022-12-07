import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;

public class OptimisticNeighborSelectorScheduler implements Runnable{
    private Peer peer;
    private Random random;

    public OptimisticNeighborSelectorScheduler(Peer peer){
        this.peer = peer;
        random = new Random();
    }

    @Override
    public void run(){
        System.out.println("INSIDE******************");
        if (Thread.currentThread().isInterrupted()) {
            System.out.println("REACHED HERE((((((((((((");
            return;
        }
        System.out.println("THISSSSSSSSSSSSS");
        List<Integer> chokedPeers = new ArrayList<>();
        for (int id : peer.getPeerInfoCfg().getPeers().keySet()){
            if ((!peer.getPreferredNeighbors().contains(id)) && (peer.getInterestedPeers().contains(id)))
                chokedPeers.add(id);
        }
        try {
            System.out.println("CHoked peers: " + chokedPeers);
            if(chokedPeers.size() > 0) {
                int optimisticNeighbor = chokedPeers.get(random.nextInt(chokedPeers.size()));
                System.out.println("OPtimistic neighbor: " + optimisticNeighbor);
                System.out.println("Peer " + optimisticNeighbor + " selected as opt unchoked neigh@@@@@@@@@@@@@@@@@");
                peer.setOptimisticNeighbor(optimisticNeighbor);
                EndPoint ep = peer.getPeerEndPoint(optimisticNeighbor);
                ep.sendMessage(Constants.MessageType.UNCHOKE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
