import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.Objects;


public class PeerClient implements Runnable {
    int id;
    List<PeerInfo> peersInfo;
    ExecutorService executorService;

    public PeerClient(int id, List<PeerInfo> peersInfo, ExecutorService executorService) {
        this.id = id;
        this.peersInfo = peersInfo;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        for(PeerInfo peerInfo: this.peersInfo) {
            if (peerInfo.getId() == id) {
                break;
            }
            try {
                // Create endPoint through client
                Socket socket = null;
                while (Objects.isNull(socket)) {
                    try {
                        socket = new Socket(peerInfo.getHostName(), peerInfo.getPort());
                    } catch (Exception e) {
                        // Error in connection
                    }
                }
                System.out.println("Peer " + this.id + " made connection to Peer " + peerInfo.getId());
                System.out.println(executorService == null);
                executorService.execute(new EndPoint(id, peerInfo.getId(), socket));
            } catch (Exception e) {
                // Error in connection
                System.out.println(e);
            }
        }
    }
}
