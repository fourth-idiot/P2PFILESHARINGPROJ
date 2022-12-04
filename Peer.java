import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {
    private final int id;
    private final CommonCfg commonCfg;
    private final PeerInfoCfg peerInfoCfg;
    private final ExecutorService executorService;
    private final Map<Integer, BitSet> bitfields = new ConcurrentHashMap<>();
    private final Set<Integer> interestedPeerList = ConcurrentHashMap.newKeySet();
    private final Set<Integer> unchokedNeighborsList = ConcurrentHashMap.newKeySet();
    private final Set<Integer> chockedNeighborsList = ConcurrentHashMap.newKeySet();
    private final AtomicInteger optimisticNeighbor = new AtomicInteger(-1);
    private final Set<Integer> completedPeersList = new HashSet<>();
    private final Map<Integer, Integer> downloadRateMap = new ConcurrentHashMap<>();
    private final Map<Integer, EndPoint> peerEndPoints = new ConcurrentHashMap<>();


    public Peer(int id, CommonCfg commoncfg, PeerInfoCfg peerInfoCfg, ExecutorService executorService) {
        this.id = id;
        this.commonCfg = commoncfg;
        this.peerInfoCfg = peerInfoCfg;
        this.executorService = executorService;

        // If the peer has file, set all bytes in the bitfield (of size equal to the number of pieces) to True.
        BitSet bitfield = new BitSet(this.commonCfg.getNumberOfPieces());
        if(this.peerInfoCfg.getPeer(this.id).getHasFile()) {
            bitfield.set(0, commoncfg.getNumberOfPieces());
        }
        this.bitfields.put(id, bitfield);
        
        // Start peer server and client
        this.executorService.execute(new PeerServer());
        this.executorService.execute(new PeerClient());
    }

    public PeerInfoCfg getPeerInfoCfg(){
        return peerInfoCfg;
    }

    public Map<Integer, BitSet> getBitfields() {
        return this.bitfields;
    }

    public Set<Integer> getUnchokedNeighborsList(){
        return this.getUnchokedNeighborsList();
    }

    public void addPeerEndPoint(int id, EndPoint endPoint)
    {
        peerEndPoints.put(id, endPoint);
    }

    public Map<Integer, EndPoint> getPeerEndPoints()
    {
        return peerEndPoints;
    }

    public EndPoint getPeerEndPoint(int id)
    {
        return peerEndPoints.get(id);
    }

    public void setOptimisticNeighbor(int optimisticNeighbor)
    {
        this.optimisticNeighbor.set(optimisticNeighbor);
    }

    public void reselectNeighbours(){
        List<Integer> peersAccordingToDownloadRate = getPeersSortedByDownLoadRate();
        unchokedNeighborsList.clear();

        if (!interestedPeerList.isEmpty()){

            int count = 0;
            int i = 0;
            while (count < commonCfg.getNumberOfPreferredNeighbors() && i < interestedPeerList.size()){
                int currentPeer = peersAccordingToDownloadRate.get(i);
                if (interestedPeerList.contains(currentPeer)){
                    unchokedNeighborsList.add(currentPeer);
                    count++;
                }
                i++;
            }
        }



    }

    public List<Integer> getPeersSortedByDownLoadRate(){
        return null;
    }

    public BitSet getBitfield(int peerId) {
        return this.bitfields.get(peerId);
    }

    public void addOrUpdateBitfield(int peerId, BitSet bitfield) {
        this.bitfields.put(peerId, bitfield);
        for (Map.Entry<Integer, BitSet> entry : this.bitfields.entrySet()) 
            System.out.println("Key = " + entry.getKey() +
                             ", Value = " + entry.getValue());
    }
    


    // Peer server
    public class PeerServer implements Runnable {
        ServerSocket serverSocket;
    
        public PeerServer() {
            System.out.println("Creating peer server");
            try {
                // Create server socket to listen for incoming connection requests
                PeerInfoCfg.PeerInfo currentPeerInfo = Peer.this.peerInfoCfg.getPeer(id);
                String hostName = currentPeerInfo.getHostName();
                int port = currentPeerInfo.getPort();
                this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(hostName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        @Override
        public void run() {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Peer " + Peer.this.id + " received a connection request");
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, socket));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Peer client
    public class PeerClient implements Runnable {
        public PeerClient() {
            System.out.println("Creating peer client");
        }
    
        @Override
        public void run() {
            for(PeerInfoCfg.PeerInfo peerInfo : Peer.this.peerInfoCfg.getPeers().values()) {
                if (peerInfo.getId() == id) {
                    break;
                }
                try {
                    Socket socket = null;
                    while (socket == null) {
                        socket = new Socket(peerInfo.getHostName(), peerInfo.getPort());
                    }
                    System.out.println("Peer " + Peer.this.id + " made a connection request to " + peerInfo.getId());
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, peerInfo.getId(), socket));
                } catch (Exception e) {
                    e.printStackTrace();;
                }
            }
        }
    }    
}
