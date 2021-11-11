package io.crdb.spring;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.crdb.spring.common.UserDTO;
import io.crdb.spring.common.UserDTOBuilder;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceRetryTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceRetryTest.class);

    private final UserService userService;
    private final UserDTOBuilder userBuilder;

    @Autowired
    public UserServiceRetryTest(UserService userService, UserDTOBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    @BeforeEach
    void setUp() {
        userService.truncate();
    }

    @Test
    void retry() {

        UserDTO user = userBuilder.buildUser();

        userService.insertUser(user);

        final int threads = Runtime.getRuntime().availableProcessors();

        logger.debug("*********************************** insert complete -- starting {} threads ***********************************", threads);

        ExecutorService updateService = Executors.newFixedThreadPool(threads, new ThreadFactoryBuilder().setNameFormat("user-update-thread-%d").build());

        CountDownLatch countDownLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            updateService.submit(() -> {
                        int timeout = RandomUtils.nextInt(2, 5);
                        logger.debug("waiting for {} seconds", timeout);
                        try {
                            userService.blocker(user.id(), () -> {
                                try {
                                    TimeUnit.SECONDS.sleep(timeout);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });
                        } finally {
                            countDownLatch.countDown();
                        }

                        try {
                            userService.updateUser(user.id());
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
            );
        }

        try {
            boolean cleanExit = countDownLatch.await(5, TimeUnit.MINUTES);

            if (!cleanExit) {
                logger.warn("waiting time elapsed before the count reached zero");
            }
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }

        updateService.shutdown();

        logger.debug("*********************************** shutdown complete ***********************************");


    }
}
