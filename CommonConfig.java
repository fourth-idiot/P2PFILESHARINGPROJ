import java.util.ArrayList;
import java.util.List;


public class CommonConfig
{
	private int numberOfPreferredNeighbors;
	private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;
	private int numberOfChunks;

    public CommonConfig() {}

    public void parse(List<String> lines)
    {
		if(lines != null && lines.size() == 6) {
			this.numberOfPreferredNeighbors = Integer.parseInt(lines.get(0).split(" ")[1]);
			this.unchokingInterval = Integer.parseInt(lines.get(1).split(" ")[1]);
			this.optimisticUnchokingInterval = Integer.parseInt(lines.get(2).split(" ")[1]);
			this.fileName = lines.get(3).split(" ")[1];
			this.fileSize = Integer.parseInt(lines.get(4).split(" ")[1]);
			this.pieceSize = Integer.parseInt(lines.get(5).split(" ")[1]);
			// Calculate the number of chunks
			double fileSizeInDouble = (double) fileSize;
			double pieceSizeInDouble = (double) pieceSize;
			this.numberOfChunks = (int) Math.ceil(fileSizeInDouble / pieceSizeInDouble);
			return;
		}
	}

	public int getNumberOfPreferredNeighbors()
    {
        return numberOfPreferredNeighbors;
    }

    public void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors)
    {
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
    }
 
    public int getUnchokingInterval()
    {
        return unchokingInterval;
    }

    public void setUnchokingInterval(int unchokingInterval)
    {
        this.unchokingInterval = unchokingInterval;
    }
    
    public int getOptimisticUnchokingInterval()
    {
        return optimisticUnchokingInterval;
    }

    public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval)
    {
        this.optimisticUnchokingInterval=optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName=fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize)
    {
        this.fileSize = fileSize;
    }
   
    public int getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(int pieceSize)
    {
        this.pieceSize = pieceSize;
    }

	public int getNumberOfChunks()
    {
		return numberOfChunks;
	}

    public void setNumberOfChunks(int numberOfChunks)
    {
        this.numberOfChunks = numberOfChunks;
    }

    @Override
    public String toString()
    {
        return "numberOfPreferredNeighbors : " + numberOfPreferredNeighbors + "\n" + 
            "unchokingInterval : " + unchokingInterval + "\n" + 
            "optimisticUnchokingInterval : " + optimisticUnchokingInterval + "\n" + 
            "fileName : " + fileName + "\n" + 
            "fileSize : " + fileSize + "\n" + 
            "pieceSize : " + pieceSize + "\n" + 
            "numberOfChunks : " + numberOfChunks + "\n" ;
    }
}
