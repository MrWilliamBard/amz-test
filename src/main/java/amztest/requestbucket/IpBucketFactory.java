package amztest.requestbucket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Component
public class IpBucketFactory {

    private final int delay;

    private final int maxRequestCount;

    private final int existenceTime;

    private final TimeUnit delayUnit;

    private final TimeUnit existenceTimeUnit;

    public IpBucketFactory(@Value("${amz-test.delay}") int delay,
                           @Value("${amz-test.delay-unit}") String delayUnit,
                           @Value("${amz-test.existence-time}") int existenceTime,
                           @Value("${amz-test.existence-time-unit}") String existenceTimeUnit,
                           @Value("${amz-test.max-request-count}") int maxRequestCount) {
        this.delay = delay;
        this.maxRequestCount = maxRequestCount;
        this.existenceTime = existenceTime;
        this.delayUnit = TimeUnit.valueOf(delayUnit);
        this.existenceTimeUnit = TimeUnit.valueOf(existenceTimeUnit);
    }

    private final Map<String, RequestBucket> HASH_MAP = new ConcurrentHashMap<>();

    public RequestBucket getOrCreateBucketByIp(String ip) {
        if (HASH_MAP.containsKey(ip)) {
            return HASH_MAP.get(ip);
        }
        RequestBucket requestBucket = new RequestBucket(convertToMilliseconds(delay, delayUnit), maxRequestCount);
        HASH_MAP.put(ip, requestBucket);
        return requestBucket;
    }

    @Scheduled(cron = "${amz-test.clean-bucket-cron}")
    private synchronized void cleanBucketFactory() {
        long existenceTimeMillis = convertToMilliseconds(existenceTime, existenceTimeUnit);
        HASH_MAP.forEach((ip, requestBucket) -> {
            if (System.currentTimeMillis() - requestBucket.getLustUpdateMillis() > existenceTimeMillis) {
                HASH_MAP.remove(ip);
            }
        });
    }

    private static long convertToMilliseconds(int count, TimeUnit timeUnit) {
        return MILLISECONDS.convert(count, timeUnit);
    }

}
