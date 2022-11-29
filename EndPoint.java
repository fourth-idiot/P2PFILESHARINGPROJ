import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class EndPoint implements Runnable {
    private final int id;
    private int peerId;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public EndPoint(int id, Socket socket) throws IOException {
        this.id = id;
        this.peerId = id;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public EndPoint(int id, int peerId, Socket socket) throws IOException {
        this.id = id;
        this.peerId = peerId;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public void run() {
        System.out.println("Creating an endpoint");
    }
}
