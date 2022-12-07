import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.MemoryType;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EndPoint implements Runnable {
    int peer1Id;
    Peer peer1;
    int peer2Id;
    Socket socket;
    ExecutorService executorService;
    ScheduledExecutorService scheduler;
    InputStream inputStream;
    OutputStream outputStream;
    Bitfield bitfield;
    BitSet peerBitfield;
    FilePieces filePieces;
    boolean handshakeInitiated = false;
    boolean choke = true;

    Logger LOGGER = LogManager.getLogger(EndPoint.class);

    public EndPoint(int peer1Id, Peer peer1, Socket socket, ExecutorService executorService, ScheduledExecutorService scheduler, Bitfield bitfield, FilePieces filePieces) throws IOException {
        this.peer1Id = peer1Id;
        this.peer1 = peer1;
        this.peer2Id = peer1Id;
        this.socket = socket;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.bitfield = bitfield;
        this.filePieces = filePieces;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public EndPoint(int peer1Id, Peer peer1, int peer2Id, Socket socket, ExecutorService executorService, ScheduledExecutorService scheduler, Bitfield bitfield, FilePieces filePieces) throws IOException {
        this.peer1Id = peer1Id;
        this.peer1 = peer1;
        this.peer2Id = peer2Id;
        this.socket = socket;
        this.executorService = executorService;
        this.scheduler = scheduler;
        this.bitfield = bitfield;
        this.filePieces = filePieces;
        this.inputStream = this.socket.getInputStream();
        this.outputStream = this.socket.getOutputStream();
    }

    public Socket getSocket() {
        return this.socket;
    }

    private void sendHandshake() throws Exception {
        this.outputStream.write(CommonUtils.getHandshakeMessage(this.peer1Id));
    }

    private void receiveHandshake() throws Exception {
        byte[] response = inputStream.readNBytes(32);
        String responseHeader = new String(Arrays.copyOfRange(response, 0, 18), StandardCharsets.UTF_8);
        System.out.println(responseHeader + ", " + Constants.HANDSHAKE_HEADER);
        int peer2Id = CommonUtils.byteArrToInt(Arrays.copyOfRange(response, 28, 32));
        // Check if the handshake response message has correct header
        if (!responseHeader.equals(Constants.HANDSHAKE_HEADER)) {
            // Invalid hanshake response message header
            throw new IllegalArgumentException(String.format("Received invalid handshake message header (%s) from {%d}", responseHeader, peer2Id));
        }
        if (this.handshakeInitiated) {
            if (peer2Id != this.peer2Id) {
                throw new IllegalArgumentException(String.format("Peer {%d} received invalid peer id (%d) in handshake message", peer1Id, peer2Id));
            }
            LOGGER.info("{}: Peer {} makes a connection to Peer {}", CommonUtils.getCurrentTime(), this.peer1Id, this.peer2Id);
        } else {
            // Update peer2Id as received
            this.peer2Id = peer2Id;
            sendHandshake();
            LOGGER.info("{}: Peer {} is connected from Peer {}", CommonUtils.getCurrentTime(), this.peer1Id, this.peer2Id);
        }
    }

    public void sendMessage(Constants.MessageType messageType) {
        try {
            this.executorService.execute(
                new MessageSender(this.socket.getOutputStream(), CommonUtils.getMessage(messageType, new byte[0]))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBitfield() {
        System.out.println("Sending bitfield");
        try {
            this.bitfield.readLock();
            byte[] bitfield = this.bitfield.getBitfield().toByteArray();
            System.out.println("Sending bitfield message to " + this.peer2Id);
            outputStream.write(CommonUtils.getMessage(Constants.MessageType.BITFIELD, bitfield));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.bitfield.readUnlock();
        }
    }

    private void receiveBitfield(int payloadLength) throws IOException {
        System.out.println("Receiving bitfield");
        BitSet peerBitfield = BitSet.valueOf(inputStream.readNBytes(payloadLength));
        this.peerBitfield = peerBitfield;
        this.peer1.addOrUpdateBitfield(this.peer2Id, this.peerBitfield);
        // Send interested message
        boolean val = this.bitfield.isInterested(this.peerBitfield);
        System.out.println(val);
        if (this.bitfield.isInterested(this.peerBitfield)) {
            System.out.println("Sending interested message to peer " + this.peer2Id);
            sendInterested();
        }
        // Map<Integer, BitSet> peerBitfields = this.peer1.getPeerBitfields();
        // for (Map.Entry<Integer, BitSet> entry : peerBitfields.entrySet()) {
        //     System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        // }
    }

    private void sendInterested() {
        executorService.execute(new MessageSender(outputStream, CommonUtils.getMessage(Constants.MessageType.INTERESTED, null)));
    }

    private void receiveInterested() {
        LOGGER.info("{}: Peer {} received the 'interested' message from {}", CommonUtils.getCurrentTime(), this.peer1Id, this.peer2Id);
        this.peer1.addInterestedPeer(peer2Id);
    }

    private void removeNotInterestedPeer() {
        peer1.removeFromInterestedPeer(peer2Id);
    }

    private void receiveChoke() {
        choke = true;
    }

    private void receiveUnchoke() {
        System.out.println("Received unchoke message");
        LOGGER.info("{}: Peer {} is unchoked by {}", CommonUtils.getCurrentTime(), this.peer1Id, this.peer2Id);
        this.choke = false;
        sendRequest();
    }

    private void sendRequest() {
        if (choke == false) {
            int nextPieceIndex = bitfield.getNextInterestedPieceIndex(peerBitfield);
            if (nextPieceIndex != -1) {
                System.out.println("Requesting piece " + nextPieceIndex);
                bitfield.addToRequestedPieces(nextPieceIndex);
                executorService.execute(
                    new MessageSender(outputStream, CommonUtils.getMessage(Constants.MessageType.REQUEST, CommonUtils.intToByteArr(nextPieceIndex)))
                );
            } else {
                sendNotInterested();
            }
        }
    }

    private void receiveRequest(int messageLength) throws IOException {
        int pieceIndex = CommonUtils.byteArrToInt(inputStream.readNBytes(messageLength));
        System.out.println("Received piece request for piece index: " + pieceIndex);
        if (this.peer1.isUnchoked(this.peer2Id)) {
            byte[] pieceByteArray = this.filePieces.getFilePiece(pieceIndex);
            byte[] pieceResponse = new byte[4 + pieceByteArray.length];
            int counter = CommonUtils.mergeByteArrays(pieceResponse, CommonUtils.intToByteArr(pieceIndex), 0);
            CommonUtils.mergeByteArrays(pieceResponse, pieceByteArray, counter);
            System.out.println("Sending piece");
            executorService.execute(new MessageSender(outputStream, CommonUtils.getMessage(Constants.MessageType.PIECE, pieceResponse)));
        }
    }

    private void sendNotInterested() {
            executorService.execute(new MessageSender(outputStream,
                CommonUtils.getMessage(Constants.MessageType.NOT_INTERESTED, null)));
    }

    private void receiveHave(int payloadLength) throws IOException {
        int pieceIndex = CommonUtils.byteArrToInt(inputStream.readNBytes(payloadLength));
        peerBitfield.set(pieceIndex);
        if(bitfield.isInterested(peerBitfield)) {
            executorService.execute(
                new MessageSender(outputStream, CommonUtils.getMessage(Constants.MessageType.INTERESTED, null))
            );
        }
    }

    // private void sendIfInterested() {
    //     if (bitfield.isInterested(peerBitfield)) {
    //         executorService.execute(
    //                 new MessageSender(outputStream, CommonUtils.getMessage(Constants.MessageType.INTERESTED, null)));
    //     }
    // }

    private void receivePiece(int msglen) throws IOException {
        System.out.println("Message length: " + msglen);
        int pieceIndex = CommonUtils.byteArrToInt(inputStream.readNBytes(4));
        byte[] pieceByteArray = inputStream.readNBytes(msglen - 4);
        System.out.println("Saving file piece");
        this.filePieces.saveFilePiece(pieceIndex, pieceByteArray);
        bitfield.setReceivedPieceIndex(pieceIndex);
        peer1.incrementDownloadRate(this.peer2Id);
        sendHave(pieceIndex);
        if (!bitfield.isInterested(peerBitfield)) {
            sendNotInterested();
        }
        if (bitfield.pieceTransferCompleted()) {
            System.out.println("Received complete file!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            this.filePieces.joinPiecesintoFile();
            sendDone();
        } else {
            sendRequest();
        }
    }

    private void sendHave(int pieceIndex) throws IOException {
        System.out.println("Broadcasting have message with piece index: " + pieceIndex);
        for (EndPoint endPoint : this.peer1.getPeerEndPoints().values()) {
            System.out.println(endPoint);
            executorService.execute(new MessageSender(endPoint.outputStream, CommonUtils.getMessage(Constants.MessageType.HAVE, CommonUtils.intToByteArr(pieceIndex))));
        }
    }

    private void sendDone() throws IOException {
        for (EndPoint endPoint : this.peer1.getPeerEndPoints().values()) {
            executorService.execute(new MessageSender(endPoint.outputStream, CommonUtils.getMessage(Constants.MessageType.COMPLETED, null)));
            // need for done Message CommonUtil.getMessage(0, MessageType.DONE, null)
        }
    }

    private void receiveDone() throws IOException {
        peer1.addCompletedPeer(peer2Id);
        if (peer1.allPeersDone()) {
            // LOGGER.info("[{}]: All peers have successfully downloaded the file. Shutting down the service.", CommonUtil.getCurrentTime());
            this.filePieces.deletePiecesDir();
            this.executorService.shutdownNow();
            this.scheduler.shutdown();
            peer1.getPeerServer().getServerSocket().close();
            peer1.closeSocket(peer2Id);
        }
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
        // Receive handshake
        try {
            receiveHandshake();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Send bitfield message
        sendBitfield();
        // Add peer endpoint
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
                                receiveChoke();
                                break;
                            case UNCHOKE:
                                System.out.println("Received unchoke message");
                                receiveUnchoke();
                                break;
                            case INTERESTED:
                                receiveInterested();
                                break;
                            case NOT_INTERESTED:
                                removeNotInterestedPeer();
                                break;
                            case HAVE:
                                System.out.println("Received have message");
                                receiveHave(payloadLength);
                                break;
                            case BITFIELD:
                                System.out.println("Received bitfield message");
                                receiveBitfield(payloadLength);
                                break;
                            case REQUEST:
                                System.out.println("Received piece request");
                                receiveRequest(payloadLength);
                                break;
                            case PIECE:
                                System.out.println("Piece received");
                                receivePiece(payloadLength);
                                break;
                            case COMPLETED:
                                System.out.println("One of the peer received the whole file");
                                receiveDone();
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
