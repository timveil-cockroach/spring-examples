package io.crdb.spring;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.concurrent.*;

@SpringBootTest(classes = JpaApplication.class)
@ActiveProfiles("test")
public class UserServiceRetryIT {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceRetryIT.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    @Autowired
    public UserServiceRetryIT(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    @BeforeEach
    void setUp() {
        userService.deleteAll();
    }

    @Test
    void retry() {

        User savedUser = userService.save(userBuilder.buildUser());

        final int threads = Runtime.getRuntime().availableProcessors();

        logger.debug("*********************************** save complete -- starting {} threads ***********************************", threads);

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("user-update-thread-%d").build();
        ExecutorService executorService = Executors.newFixedThreadPool(threads, namedThreadFactory);

        CountDownLatch countDownLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                        try {
                            savedUser.setUpdatedTimestamp(ZonedDateTime.now());
                            userService.save(savedUser);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
            );
        }

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
