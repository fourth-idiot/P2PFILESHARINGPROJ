import java.util.Map;
import java.util.concurrent.ExecutorService;


public class PreferredNeighborsSelectorScheduler implements Runnable {
    private final ExecutorService executorService;
    private Peer peer;

    public PreferredNeighborsSelectorScheduler(ExecutorService executorService, Peer peer){
        this.executorService = executorService;
        this.peer = peer;
    }

    @Override
    public void run(){
        System.out.println("Preferred neigh handlers scheduler");
        if (Thread.currentThread().isInterrupted())
            return;
        this.peer.reselectNeighbours();
        for (Map.Entry<Integer, EndPoint> entry : peer.getPeerEndPoints().entrySet()) {
            Integer peerId = entry.getKey();
            EndPoint endPoint = entry.getValue();
            if (peer.getPreferredNeighbors().contains(peerId)) {
                endPoint.sendMessage(Constants.MessageType.UNCHOKE);
            } else if (peer.getOptimisticNeighbor().get() == peerId) {
                continue;
            } else {
                endPoint.sendMessage(Constants.MessageType.CHOKE);
            }
        }
    }
}
