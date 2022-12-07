import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.time.LocalDateTime;


public class Helper {
    public static byte[] getMessage(Constants.MessageType messageType, byte[] messagePayload) {
        int payloadLength = messagePayload != null ? messagePayload.length : 0;
        byte[] messageLength = intToByteArr(payloadLength);
        byte[] message = new byte[5 + payloadLength];
        for (int i = 0; i < message.length; i++) {
            if (i < messageLength.length) {
                message[i] = messageLength[i];
                continue;
            } else if (i == messageLength.length) {
                message[i] = (byte) messageType.getValue();
            } else {
                message[i] = messagePayload[i - 5];
            }
        }
        return message;
    }

    public static byte[] getHandshakeMessage(int id) {
        String header = Constants.HANDSHAKE_HEADER;
        byte[] headerBytes = header.getBytes();

        String zeroes = Constants.ZERO_BITS_HANDSHAKE;
        byte[] zeroBytes = zeroes.getBytes();

        byte[] idBytes = intToByteArr(id);

        int messageLength = headerBytes.length + zeroBytes.length + idBytes.length;
        byte[] message = new byte[messageLength];
        for (int i = 0; i < message.length; i++) {
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

    public static int mergeByteArr(byte[] arr1, byte[] arr2, int start) {
        for (byte val : arr2) {
            arr1[start++] = val;

        }
        return start;
    }

    public static String getCurrentTime() {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").format(LocalDateTime.now());
    }

    public static int mergeByteArrays(byte[] arr1, byte[] arr2, int start)
    {
        for (byte byteData : arr2)
        {
            arr1[start++] = byteData;
        }

        return start;
    }

    public static void deleteDirectory(String path) throws IOException
    {
        Files
        .walk(Paths.get(path))
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
}