import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.BitSet;

public class Bitfield {
    private final BitSet bitfield;
    private final Set<Integer> requestedPieces = ConcurrentHashMap.newKeySet();
    private final DelayQueue<PieceIndex> delayQueue = new DelayQueue<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();

    public Bitfield(BitSet bitfield) {
        this.bitfield = bitfield;
    }

    public BitSet getBitfield() {
        return bitfield;
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

    private void readUnlock() {
        readLock.unlock();
    }

    public void readLock() {
        readLock.lock();
    }

    public DelayQueue<PieceIndex> getDelayQueue()
    {
        return delayQueue;
    }

    public void removeTimedOutPieceIndex(int pieceIndex)
    {
        requestedPieces.remove(pieceIndex);
    }
}
