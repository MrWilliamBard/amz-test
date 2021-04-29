package amztest.rest;

import amztest.requestbucket.IpBucketFactory;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.OK;

@Execution(ExecutionMode.CONCURRENT)
class AmzTestControllerTest {


    private static final int DELAY = 1;

    private static final String DELAY_UNIT = TimeUnit.MINUTES.name();

    private static final int EXISTENCE_TIME = 1;

    private static final String EXISTENCE_TIME_UNIT = TimeUnit.HOURS.name();

    private static final int REQUEST_COUNT = 60;



    private static final int DELAY_IN_SECONDS = DELAY * 60;

    private static final int DEFAULT_TIMEOUT = DELAY_IN_SECONDS / 12;

    private static final int PAUSE_IN_SECONDS = DELAY_IN_SECONDS / 2;

    private static final int THREAD_POOL_SIZE = 8;

    private final IpBucketFactory ipBucketFactory = new IpBucketFactory(DELAY,
            DELAY_UNIT,
            EXISTENCE_TIME,
            EXISTENCE_TIME_UNIT,
            REQUEST_COUNT);

    private final AmzTestController amzTestController = new AmzTestController(ipBucketFactory);

    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @RepeatedTest(THREAD_POOL_SIZE)
    void testThread() throws ExecutionException, InterruptedException, TimeoutException {
        String ip = RandomString.make(9);

        Callable<List<HttpStatus>> successCallable = getCallablesForTest(ip, PAUSE_IN_SECONDS);

        Future<List<HttpStatus>> successListFutureOne = executorService.submit(successCallable);
        List<HttpStatus> statusList = successListFutureOne.get(PAUSE_IN_SECONDS + DEFAULT_TIMEOUT, SECONDS);
        assertThat(statusList).containsOnly(OK);

        Callable<List<HttpStatus>> failedCallable = getCallablesForTest(ip, 0);
        Future<List<HttpStatus>> failedListFutureOne = executorService.submit(failedCallable);
        List<HttpStatus> failedStatusList = failedListFutureOne.get(DEFAULT_TIMEOUT, SECONDS);
        assertThat(failedStatusList).containsOnly(BAD_GATEWAY);

        SECONDS.sleep(PAUSE_IN_SECONDS);

        Callable<List<HttpStatus>> mixedCallable = getCallablesForTest(ip, 0);

        Future<List<HttpStatus>> mixedStatusListFutureTwo = executorService.submit(mixedCallable);
        List<HttpStatus> mixedStatusList = mixedStatusListFutureTwo.get(DEFAULT_TIMEOUT, SECONDS);
        long failedCount = mixedStatusList.stream()
                .filter(httpStatus -> httpStatus.equals(BAD_GATEWAY))
                .count();
        long successCount = mixedStatusList.stream()
                .filter(httpStatus -> httpStatus.equals(OK))
                .count();
        assertThat(failedCount).isEqualTo(REQUEST_COUNT / 2);
        assertThat(successCount).isEqualTo(REQUEST_COUNT / 2);
    }

    private Callable<List<HttpStatus>> getCallablesForTest(String ip, int pauseInSeconds) {
        return () -> {
            HttpServletRequest httpServletRequest = spy(HttpServletRequest.class);
            when(httpServletRequest.getRemoteAddr()).thenReturn(ip);
            List<HttpStatus> statusList = new LinkedList<>();
            for (int j = 0; j < REQUEST_COUNT / 2; j++) {
                statusList.add(amzTestController.test(httpServletRequest).getStatusCode());
            }
            SECONDS.sleep(pauseInSeconds);
            for (int j = REQUEST_COUNT / 2; j < REQUEST_COUNT; j++) {
                statusList.add(amzTestController.test(httpServletRequest).getStatusCode());
            }
            return statusList;
        };
    }
}