import java.io.OutputStream;
import java.net.SocketException;

public class MessageSender implements Runnable {

    private final OutputStream outputStream;
    private final byte[] msg;

    public MessageSender(OutputStream outputStream, byte[] msg) {
        this.outputStream = outputStream;
        this.msg = msg;
    }

    @Override
    public void run() {

        try {
            if (Thread.currentThread().isInterrupted())
                return;

            synchronized(outputStream){
                Thread.sleep(15);
                outputStream.write(msg);
            }
        } catch (InterruptedException | SocketException e){
            return;
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
}
