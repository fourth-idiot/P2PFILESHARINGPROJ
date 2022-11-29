import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class EndPoint implements Runnable {
    private final int id;
    private int peerId;
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private boolean handshakeInitiated = false;

    public EndPoint(int id, Socket socket) throws IOException {
        this.id = id;
        this.peerId = id;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        System.out.println("Created an endpoint for peer " + this.id + " through server code");
    }

    public EndPoint(int id, int peerId, Socket socket) throws IOException {
        this.id = id;
        this.peerId = peerId;
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        System.out.println("Created an endpoint for peer " + this.id + " through client code");
    }

    private void sendHandshake() throws Exception {
        System.out.println("Peer " + this.id + " sending handshake message to Peer " + this.peerId);
        outputStream.write(CommonUtils.getHandshakeMessage(this.id));
    }

    private void receiveHandshake() throws Exception {
        // Parse received response
        byte[] response = inputStream.readNBytes(32);
        String responseHeader = new String(Arrays.copyOfRange(response, 0, 18), StandardCharsets.UTF_8);
        int responseId = CommonUtils.byteArrToInt(Arrays.copyOfRange(response, 28, 32));

        // Check if the response header is correct
        if (!Constants.HANDSHAKE_HEADER.equals(responseHeader)) {
            System.out.println("Invalid handshake header");
        }

        if (this.handshakeInitiated) {
            // If the handshake was already initiated,
            // check if the received peer id is correct
            System.out.println(responseId + ", " + peerId);
            if (responseId != peerId) {
                System.out.println("Invalid peer id");
            }
        } else {
            peerId = responseId;
            sendHandshake();
        }

        System.out.println("Peer " + this.id + " received handshake message from Peer " + responseId);
    }

    @Override
    public void run() {
        System.out.println("Inside this...................");
        // Running an endpoint
        if(this.id != this.peerId) {
            try {
                sendHandshake();
                this.handshakeInitiated = true;
            } catch (Exception e) {
                // Error in sending handshake message
            }
        }

        try {
            receiveHandshake();
        } catch (Exception e) {
            // Error in receiving hashake message
        }
    }
}
