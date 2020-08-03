package io.crdb.spring;

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
class UserServiceLoopTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceLoopTest.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    @Autowired
    public UserServiceLoopTest(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }


    @Test
    void looping() {

        int nThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        CountDownLatch countDownLatch = new CountDownLatch(nThreads);

        logger.info("number of threads {}", nThreads);


        for (int i = 0; i < nThreads; i++) {
            executorService.submit(() -> {
                try {
                    for (int i1 = 0; i1 < 100; i1++) {
                        User user = userBuilder.buildUser();
                        logger.debug("calling save #{}", i1);
                        userService.save(user);
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            boolean cleanExit = countDownLatch.await(1, TimeUnit.HOURS);

            if (!cleanExit) {
                logger.warn("waiting time elapsed before the count reached zero");
            }
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }

        executorService.shutdown();

    }


}