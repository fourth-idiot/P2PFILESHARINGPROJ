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
        }
    }

    @Override
    public void run() {
        try {
            while (true) {   
                Socket socket = serverSocket.accept();
                this.executorService.execute(new EndPoint(this.id, socket));
            }
        } catch (Exception e) {
            // Error in creating server socket
        }
    }
}
