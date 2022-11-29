import java.nio.ByteBuffer;


public class CommonUtils {
    public static byte[] getHandshakeMessage(int id) {
        String header = Constants.HANDSHAKE_HEADER;
        byte[] headerBytes = header.getBytes();
        
        String zeroes = Constants.ZERO_BITS_HANDSHAKE;
        byte[] zeroBytes = zeroes.getBytes();
        
        byte[] idBytes = intToByteArr(id);
        
        int messageLength = headerBytes.length + zeroBytes.length + idBytes.length;
        byte[] message = new byte[messageLength];
        for (int i = 0; i < messageLength; i++) {
            if (i < headerBytes.length) {
                message[i] = headerBytes[i];
            } else if (i < headerBytes.length + zeroBytes.length) {
                message[i] = zeroBytes[i - headerBytes.length];
            } else {
                message[i] = idBytes[i - headerBytes.length - zeroBytes.length];
            }
        }
        return message;
    }

    public static byte[] intToByteArr(int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }

    public static int byteArrToInt(byte[] byteArr) {
        return ByteBuffer.wrap(byteArr).getInt();
    }
}