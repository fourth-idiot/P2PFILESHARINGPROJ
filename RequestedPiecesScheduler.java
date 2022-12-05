import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;

import java.util.BitSet;
import java.util.Map;;

public class RequestedPiecesScheduler implements Runnable{

    private final int id;
    private final ExecutorService executorService;
    private Peer peer;
    private final Bitfield bitfield;

    public RequestedPiecesScheduler(int id, ExecutorService executorService, Peer peer) {
        this.id = id;
        this.executorService = executorService;
        this.peer = peer;
        this.bitfield = peer.getBitfieldNew(id);
        
    }

    @Override
    public void run() {

        try{
            if (Thread.currentThread().isInterrupted())
                return;

            DelayQueue<PieceIndex> piecesRequested = bitfield.getDelayQueue();
            
            PieceIndex expiredPieceIndex = piecesRequested.poll();

            while(Objects.nonNull(expiredPieceIndex)){
                bitfield.removeTimedOutPieceIndex(id);

                for (Map.Entry<Integer, Bitfield> entry : peer.getBitfieldMap().entrySet()){
                    BitSet bitset = entry.getValue().getBitfield();
                    if (bitset.get(expiredPieceIndex.getIndex())){
                        EndPoint ep = peer.getPeerEndPoint(entry.getKey());
                        ep.sendMessage(Constants.MessageType.INTERESTED, executorService);
                    }
                }
                
            }
            
            
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }

}
