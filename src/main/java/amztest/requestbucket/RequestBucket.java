package amztest.requestbucket;

import java.time.LocalDateTime;
import java.util.Arrays;

public class RequestBucket {

    private int currentRequestCount;

    private final int maxRequestCount;

    private final long delay;

    private final long[] millisArray;

    private long lustUpdateMillis;


    public RequestBucket(long delay, int maxRequestCount) {
        this.currentRequestCount = 0;
        this.delay = delay;
        this.millisArray = new long[maxRequestCount];
        this.maxRequestCount = maxRequestCount;
        this.lustUpdateMillis = System.currentTimeMillis();
        Arrays.fill(millisArray, Long.MAX_VALUE);
    }

    public synchronized boolean add() {
        if (cleanBucket()) {
            millisArray[currentRequestCount++] = System.currentTimeMillis();
            lustUpdateMillis = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private boolean cleanBucket() {
        long previousCount = currentRequestCount;
        for (int i = 0; i < millisArray.length; i++) {
            if (System.currentTimeMillis() - millisArray[i] > delay) {
                millisArray[i] = Long.MAX_VALUE;
                currentRequestCount--;
            } else break;
        }
        if (currentRequestCount != previousCount) {
            Arrays.sort(millisArray);
        }
        return currentRequestCount < maxRequestCount;
    }

    public long getLustUpdateMillis() {
        return lustUpdateMillis;
    }
}
