package io.crdb.spring;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.crdb.spring.common.UserDTO;
import io.crdb.spring.common.UserDTOBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;

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

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("user-update-thread-%d").build();
        ExecutorService executorService = Executors.newFixedThreadPool(threads, namedThreadFactory);

        CountDownLatch countDownLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executorService.submit(() -> {
                        try {
                            userService.updateUser(user.getId());
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
