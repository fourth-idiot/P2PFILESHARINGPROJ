import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ExecutorService;

// import Constants.MessageType;

public class EndPoint implements Runnable {
    int peer1Id;
    Peer peer1;
    int peer2Id;
    Socket socket;
    InputStream inputStream;
    OutputStream outputStream;
    Bitfield bfUtil;
    ExecutorService executorService;
    // Peer peer;
    boolean handshakeInitiated = false;
    BitSet bitfield = null;
    boolean choke = true;

    public EndPoint(int peer1Id, Peer peer1, Socket socket) throws IOException {
        System.out.println("Creating endpoint instance between " + peer1Id + ", " + peer1Id);
        this.peer1Id = peer1Id;
        this.peer1 = peer1;
        this.peer2Id = peer1Id;
        this.socket = socket;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public EndPoint(int peer1Id, Peer peer1, int peer2Id, Socket socket) throws IOException {
        System.out.println("Creating endpoint instance between " + peer1Id + ", " + peer2Id);
        this.peer1Id = peer1Id;
        this.peer1 = peer1;
        this.peer2Id = peer2Id;
        this.socket = socket;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    private void sendHandshake() throws Exception {
        System.out.println("Peer " + this.peer1Id + " sending handshake request to Peer " + this.peer2Id);
        this.outputStream.write(CommonUtils.getHandshakeMessage(this.peer1Id));
    }

    private void receiveHandshake() throws Exception {
        byte[] response = inputStream.readNBytes(32);
        String responseHeader = new String(Arrays.copyOfRange(response, 0, 18), StandardCharsets.UTF_8);
        int peer2Id = CommonUtils.byteArrToInt(Arrays.copyOfRange(response, 28, 32));

        // Check if the handshake response message has correct header
        if (responseHeader != Constants.HANDSHAKE_HEADER) {
            // Invalid hanshake response message header
        }

        if (this.handshakeInitiated) {
            if (peer2Id != this.peer2Id) {
                System.out.println("Invalid peer2Id");
            }
        } else {
            // Update peer2Id as received
            this.peer2Id = peer2Id;
            sendHandshake();
        }

        System.out.println("Peer " + this.peer1Id + " received handshake message from Peer " + this.peer2Id);
    }

    private void sendBitfield() {
        try {
            byte[] bitfield = this.peer1.getBitfield(this.peer1Id).toByteArray();
            outputStream.write(CommonUtils.getMessage(Constants.MessageType.BITFIELD, bitfield));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bitfieldHandler(int payloadLength) throws IOException {
        BitSet bitfield = BitSet.valueOf(inputStream.readNBytes(payloadLength));
        this.peer1.addOrUpdateBitfield(this.peer2Id, bitfield);
    }

    private void addInterestedPeer() {
        peer1.addInterestedPeer(peer2Id);
    }

    private void removeNotInterestedPeer() {
        peer1.removeFromInterestedPeer(peer2Id);
    }

    private void chokeHandler() {
        choke = true;
    }

    private void unchokeHandler() {
        choke = false;
        requestPiece();
        // sendPieceRequest();
    }

    private void requestPiece() {
        if (choke == false) {
            int nextPieceIdx = bfUtil.getNextInterestedPieceIndex(bitfield);
            if (nextPieceIdx != -1) {
                executorService
                        .execute(new MessageSender(outputStream,
                                CommonUtils.getMessage(Constants.MessageType.NOT_INTERESTED, null)));
            }
        }
    }

    private void haveHandler(int payloadLength) throws IOException {
        int pieceIndex = CommonUtils.byteArrToInt(inputStream.readNBytes(payloadLength));
        bitfield.set(pieceIndex);
        // sendIfinterested();

    }

    @Override
    public void run() {
        // Initiate handshake only if both the peerIds are known
        if (this.peer1Id != this.peer2Id) {
            try {
                sendHandshake();
                this.handshakeInitiated = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            receiveHandshake();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send bitfield message
        sendBitfield();

        peer1.addPeerEndPoint(peer2Id, this);

        // Keep listening for other messages
        try {
            while (true) {
                byte[] message = inputStream.readNBytes(5);
                if (message.length > 0) {
                    int payloadLength = CommonUtils.byteArrToInt(Arrays.copyOfRange(message, 0, 4));
                    Constants.MessageType messageType = Constants.MessageType.getByValue((int) message[4]);
                    if (messageType != null) {
                        switch (messageType) {
                            case CHOKE:
                                chokeHandler();
                                break;
                            case UNCHOKE:
                                unchokeHandler();
                                break;
                            case INTERESTED:
                                addInterestedPeer();
                                break;
                            case NOT_INTERESTED:
                                removeNotInterestedPeer();
                                break;
                            case HAVE:
                                // haveHandler();
                                break;
                            case BITFIELD:
                                System.out.println("Received bitfield message");
                                bitfieldHandler(payloadLength);
                                break;
                            case REQUEST:
                                break;
                            case PIECE:
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(Constants.MessageType messageType, ExecutorService executorService){
        try {
            executorService.execute(new MessageSender(socket.getOutputStream(), CommonUtils.getMessage(messageType, new byte[0])));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
