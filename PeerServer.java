import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class PeerServer implements Runnable {
    int id;
    String hostName;
    int port;
    ServerSocket serverSocket;
    ExecutorService executorService;

    public PeerServer(int id, String hostName, int port, ExecutorService executorService) {
        try {
            this.id = id;
            this.hostName = hostName;
            this.port = port;
            this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(hostName));
            this.executorService = executorService;
        } catch (Exception e) {
            // Error in creating server socket
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Starting peerServer for peer: " + this.id);
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Peer " + this.id + " received a TCP socket request");
                System.out.println(this.executorService == null);
                this.executorService.execute(new EndPoint(this.id, socket));
            }
        } catch (Exception e) {
            // Error in creating server socket
            e.printStackTrace();
        }
    }
}
