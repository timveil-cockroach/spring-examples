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

        logger.info("number of threads {}", nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.submit(() -> {
                while (true) {
                    User user = userBuilder.buildUser();
                    userService.save(user);
                }
            });
        }


        executorService.shutdown();

        try {
            executorService.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}