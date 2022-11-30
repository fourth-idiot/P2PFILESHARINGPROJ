import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class Peer {
    private final int id;
    private final CommonCfg commonCfg;
    private final PeerInfoCfg peerInfoCfg;
    private final ExecutorService executorService;
    private final Map<Integer, BitSet> bitfields = new ConcurrentHashMap<>();

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

    public Map<Integer, BitSet> getBitfields() {
        return this.bitfields;
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
