import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;


public class PreferredNeighborsSelectorScheduler implements Runnable {

    private final int id;
    private final ExecutorService executorService;
    private Peer peer;

    public PreferredNeighborsSelectorScheduler(int id, ExecutorService executorService, Peer peer){
        this.id = id;
        this.executorService = executorService;
        this.peer = peer;
    }

    @Override
    public void run(){

        if (Thread.currentThread().isInterrupted())
            return;

        peer.reselectNeighbours();

        if (!peer.getUnchokedNeighborsList().isEmpty()){

           // sendMessage(peer.getUnchokedNeighborsList(), Constants.MessageType.UNCHOKE);
           for (int id : peer.getUnchokedNeighborsList()){
            EndPoint ep = peer.getPeerEndPoint(id);
            ep.sendMessage(Constants.MessageType.UNCHOKE, executorService);
           }

        }

        
    }


    
}
