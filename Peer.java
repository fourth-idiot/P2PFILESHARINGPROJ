import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {
    private final int id;
    private final CommonCfg commonCfg;
    private final PeerInfoCfg peerInfoCfg;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final FilePieces filePieces;
    private final Bitfield bitfield;
    private final Map<Integer, BitSet> peerBitfields = new ConcurrentHashMap<>();
    private final Set<Integer> interestedPeers = ConcurrentHashMap.newKeySet();
    private final Set<Integer> preferredNeighbors = ConcurrentHashMap.newKeySet();
    private final Set<Integer> chockedNeighborsList = ConcurrentHashMap.newKeySet();
    private final AtomicInteger optimisticNeighbor = new AtomicInteger(-1);
    private final Set<Integer> completedPeersList = new HashSet<>();
    private final Map<Integer, Integer> downloadRateMap = new ConcurrentHashMap<>();
    private final Map<Integer, EndPoint> peerEndPoints = new ConcurrentHashMap<>();
    private final Set<Integer> completedPeers = new HashSet<>();
    private final PeerServer peerServer;
    private final PeerClient peerClient;

    public Peer(int id, CommonCfg commoncfg, PeerInfoCfg peerInfoCfg, ExecutorService executorService, ScheduledExecutorService scheduler) {
        this.id = id;
        this.commonCfg = commoncfg;
        this.peerInfoCfg = peerInfoCfg;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.filePieces = new FilePieces(this.id, this.commonCfg);

        // If the peer has file, set all bytes in the bitfield
        // (of size equal to the number of pieces) to True.
        // Also, break file into pieces
        BitSet bitfield = new BitSet(this.commonCfg.getNumberOfPieces());
        if (this.peerInfoCfg.getPeer(this.id).getHasFile()) {
            bitfield.set(0, commoncfg.getNumberOfPieces());
            this.filePieces.splitFileintoPieces();
        }
        this.bitfield = new Bitfield(bitfield, commoncfg);

        // Start peer server and client
        this.peerServer = new PeerServer();
        this.executorService.execute(peerServer);
        this.peerClient = new PeerClient();
        this.executorService.execute(this.peerClient);
    }

    public PeerInfoCfg getPeerInfoCfg() {
        return peerInfoCfg;
    }

    public Map<Integer, BitSet> getPeerBitfields() {
        return this.peerBitfields;
    }

    public Set<Integer> getPreferredNeighbors() {
        return this.preferredNeighbors;
    }

    public void addPeerEndPoint(int peerId, EndPoint endPoint) {
        System.out.println(peerId);
        this.peerEndPoints.put(peerId, endPoint);
        this.downloadRateMap.put(peerId, 0);
    }

    public Map<Integer, EndPoint> getPeerEndPoints() {
        return this.peerEndPoints;
    }

    public EndPoint getPeerEndPoint(int peerId) {
        return peerEndPoints.get(peerId);
    }

    public void setOptimisticNeighbor(int optimisticNeighbor) {
        this.optimisticNeighbor.set(optimisticNeighbor);
    }

    public Set<Integer> getInterestedPeers() {
        return this.interestedPeers;
    }

    public AtomicInteger getOptimisticNeighbor() {
        return this.optimisticNeighbor;
    }

    public boolean isUnchoked(int peerId) {
        return preferredNeighbors.contains(peerId) || optimisticNeighbor.get() == peerId;
    }

    public void addCompletedPeer(int peerId) {
        this.completedPeers.add(peerId);
    }

    public synchronized boolean allPeersDone()
    {
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        Set<Integer> peerIds = peerInfoCfg.getPeers().keySet();
        if (bitfield.allPiecesReceived()) {
            peerIds.remove(id);
        }
        peerIds.removeAll(completedPeers);
        return peerIds.size() == 0;
    }

    public void reselectNeighbours() {
        // Sort peers by decreasing order of their download rates
        List<Integer> peersAccordingToDownloadRate = getPeersSortedByDownloadRate();
        // Reset unchoked peer's list and download rates
        this.preferredNeighbors.clear();
        System.out.println(this.preferredNeighbors);
        for (int peerId : downloadRateMap.keySet()){
            downloadRateMap.put(peerId, 0);
        }
        System.out.println(downloadRateMap);
        // Select numberOfPreferredNeighbors with highest download rates
        // and which are interested in peer's data
        int count = 0;
        int i = 0;
        while (count < commonCfg.getNumberOfPreferredNeighbors() && i < this.interestedPeers.size()) {
            int currentPeer = peersAccordingToDownloadRate.get(i);
            if (this.interestedPeers.contains(currentPeer)) {
                this.preferredNeighbors.add(currentPeer);
                count++;
            }
            i++;
        }
        System.out.println("Neighbors reselected: " + this.preferredNeighbors);
    }

    public List<Integer> getPeersSortedByDownloadRate() {
        List<Map.Entry<Integer, Integer>> sortedDownloadRateMap = new ArrayList<>(downloadRateMap.entrySet());
        sortedDownloadRateMap.sort(Map.Entry.comparingByValue());
        List<Integer> sortedPeers = new ArrayList<>();
        for(Map.Entry<Integer, Integer> entry : sortedDownloadRateMap) {
            sortedPeers.add(entry.getKey());
        }
        return sortedPeers;
    }

    public Bitfield getBitfield() {
        return this.bitfield;
    }

    public void addOrUpdateBitfield(int peerId, BitSet bitfield) {
        this.peerBitfields.put(peerId, bitfield);
    }

    public void incrementDownloadRate(int peerId) {
        this.downloadRateMap.put(peerId, this.downloadRateMap.get(peerId) + 1);
        System.out.println(this.downloadRateMap);
    }

    public void addInterestedPeer(int peerId) {
        interestedPeers.add(peerId);
    }

    public void removeFromInterestedPeer(int peerId) {
        interestedPeers.remove(peerId);
    }

    public Peer.PeerServer getPeerServer() {
        return this.peerServer;
    }

    public void closeSocket(int peerId) throws IOException {
        peerEndPoints.get(peerId).getSocket().close();
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

        public ServerSocket getServerSocket() {
            return this.serverSocket;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Peer " + Peer.this.id + " received a connection request");
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, socket, Peer.this.executorService, Peer.this.scheduler, Peer.this.bitfield, Peer.this.filePieces));
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
            for (PeerInfoCfg.PeerInfo peerInfo : Peer.this.peerInfoCfg.getPeers().values()) {
                if (peerInfo.getId() == id) {
                    break;
                }
                try {
                    Socket socket = null;
                    while (socket == null) {
                        socket = new Socket(peerInfo.getHostName(), peerInfo.getPort());
                    }
                    System.out.println("Peer " + Peer.this.id + " made a connection request to " + peerInfo.getId());
                    Peer.this.executorService.execute(new EndPoint(id, Peer.this, peerInfo.getId(), socket, Peer.this.executorService, Peer.this.scheduler, Peer.this.bitfield, Peer.this.filePieces));
                } catch (Exception e) {
                    e.printStackTrace();
                    ;
                }
            }
        }
    }
}
