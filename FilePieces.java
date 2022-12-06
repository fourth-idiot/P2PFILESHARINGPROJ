import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FilePieces {
    private final String tempFilesPath;
    private final String finalFilePath;
    private final CommonCfg commonConfig;
    public static final String TEMP = "temp";
    public static final String TEMP_EXT = ".tmp";
    public static final String PEER = "peer_";

    public FilePieces(int id, CommonCfg commonCfg) {
        this.commonConfig = commonCfg;
        String basePath = System.getProperty(Constants.WORKING_DIR) + File.separator + PEER + id
                + File.separator;
        tempFilesPath = basePath + TEMP + File.separator;
        finalFilePath = basePath + this.commonConfig.getFileName();
    }

    public byte[] getFilePiece(int pieceIndex) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(
                tempFilesPath + TEMP + pieceIndex + TEMP_EXT)) {
            int length = (int) fileInputStream.getChannel().size();
            byte[] pieceData = new byte[length];
            int readBytes = 0;

            while (readBytes < length) {
                readBytes += fileInputStream.read(pieceData, readBytes, length);
            }

            return pieceData;
        } catch (IOException e) {
            // LOGGER.error("Error while reading file data.", e);
            throw e;
        }
    }

    public void savePiece(int pieceIndex, byte[] pieceData) throws IOException {
        String piecePath = tempFilesPath + TEMP + pieceIndex + TEMP_EXT;

        try {
            Files.createDirectories(Paths.get(tempFilesPath));
        } catch (IOException e) {
            // LOGGER.error("Error while creating temp directory", e);
        }

        try (FileOutputStream os = new FileOutputStream(piecePath)) {
            os.write(pieceData);
        } catch (IOException e) {
            // LOGGER.error("Error while saving piece data with index {}", pieceIndex, e);
            throw e;
        }
    }

    public void joinAndSavePieces() throws IOException {
        try (FileOutputStream os = new FileOutputStream(finalFilePath, true)) {
            for (int i = 0; i < commonConfig.getNumberOfPieces(); i++) {
                os.write(Files.readAllBytes(Paths.get(tempFilesPath + TEMP + i + TEMP_EXT)));
            }
        } catch (IOException e) {
            // LOGGER.error("Error while saving the final file", e);
            throw e;
        }
    }
}
