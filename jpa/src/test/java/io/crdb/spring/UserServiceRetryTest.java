package io.crdb.spring;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceRetryTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceRetryTest.class);

    private final UserService userService;
    private final UserBuilder userBuilder;
    private final DataSource dataSource;

    @Autowired
    public UserServiceRetryTest(UserService userService, UserBuilder userBuilder, DataSource dataSource) {
        this.userService = userService;
        this.userBuilder = userBuilder;
        this.dataSource = dataSource;
    }

    @Test
    void retry() {

        User savedUser = userService.save(userBuilder.buildUser());

        logger.debug("*********************************** save complete -- starting threads ***********************************");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch countDownLatch = new CountDownLatch(2);

        // simulates a long running update transaction.  this transaction opens before `update` thread and closes after `update` thread commits
        Runnable block = () -> {
            logger.debug("*********************************** starting blocking ***********************************");

            try {
                savedUser.setUpdatedTimestamp(ZonedDateTime.now());
                userService.forceRetry(savedUser, 1, 5);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            } finally {
                countDownLatch.countDown();
                logger.debug("*********************************** finished blocking ***********************************");
            }
        };

        // is an acutal update that supports retry logic
        Runnable update = () -> {

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }

            logger.debug("*********************************** starting retryable ***********************************");

            try {
                savedUser.setUpdatedTimestamp(ZonedDateTime.now());
                userService.save(savedUser);
            } finally {
                countDownLatch.countDown();
                logger.debug("*********************************** finished retryable ***********************************");
            }
        };

        executorService.submit(block);
        executorService.submit(update);

        try {
            boolean cleanExit = countDownLatch.await(30, TimeUnit.SECONDS);

            if (!cleanExit) {
                logger.warn("waiting time elapsed before the count reached zero");
            }
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }

        executorService.shutdown();

        logger.debug("*********************************** shutdown complete ***********************************");

    }
}
