import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.BitSet;

public class Bitfield {
    private final BitSet bitfield;
    private final int numberOfPieces;
    private final Set<Integer> requestedPieces = ConcurrentHashMap.newKeySet();
    private final DelayQueue<PieceIndex> delayQueue = new DelayQueue<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public Bitfield(BitSet bitfield, CommonCfg commonCfg) {
        this.bitfield = bitfield;
        this.numberOfPieces = commonCfg.getNumberOfPieces();
    }

    public BitSet getBitfield() {
        return bitfield;
    }

    public boolean isInterested(BitSet peerBitField) {
        try {
            readLock();
            return getNextClearIndex(peerBitField) != -1;
        } finally {
            readUnlock();
        }
    }

    public int getFirstClearIndexFromBitfields(BitSet bf1, BitSet bf2, int fromIdx) {
        int index = fromIdx;
        while ((index = bf1.nextClearBit(index)) != -1) {
            if (bf2.get(index)) {
                break;
            } else if (index >= bf1.length()) {
                index = -1;
                break;
            }
            index++;
        }
        return index;
    }

    private int getNextClearIndex(BitSet peerBitField) {
        int index = -1;
        do {
            index = getFirstClearIndexFromBitfields(bitfield, peerBitField, index + 1);
        } while (requestedPieces.contains(index));
        return index;

    }

    public int getNextInterestedPieceIndex(BitSet peerBitField) {
        try {
            readLock();
            int nextPieceIdx = getNextClearIndex(peerBitField);
            if (nextPieceIdx != -1) {
                requestedPieces.add(nextPieceIdx);
                delayQueue.add(new PieceIndex(nextPieceIdx));
            }
            return nextPieceIdx;
        } finally {
            readUnlock();
        }
    }

    public void removeReceivedPieceIndex(int pieceIndex) {
        try {
            writeLock();
            bitfield.set(pieceIndex);
            requestedPieces.remove(pieceIndex);
        } finally {
            writeUnlock();
        }
    }

    public boolean allPiecesReceived() {
        int nextClearIndex = bitfield.nextClearBit(0);
        return nextClearIndex == -1 || nextClearIndex >= numberOfPieces;
    }

    private void readUnlock() {
        readLock.unlock();
    }

    public void readLock() {
        readLock.lock();
    }

    public void writeLock() {
        writeLock.lock();
    }

    public void writeUnlock() {
        writeLock.unlock();
    }

    public DelayQueue<PieceIndex> getDelayQueue() {
        return delayQueue;
    }

    public void removeTimedOutPieceIndex(int pieceIndex) {
        requestedPieces.remove(pieceIndex);
    }
}
