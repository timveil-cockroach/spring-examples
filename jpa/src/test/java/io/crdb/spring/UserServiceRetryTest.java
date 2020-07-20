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

        User user = userBuilder.buildUser();

        userService.save(user);

        logger.debug("*********************************** save complete -- starting threads ***********************************");

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch countDownLatch = new CountDownLatch(2);

        /*
        Runnable select = () -> {

            logger.debug("*********************************** starting blocking update ***********************************");

            try (Connection connection = DataSourceUtils.getConnection(dataSource)) {

                connection.setAutoCommit(false);

                Savepoint savepoint = connection.setSavepoint("holding select");

                try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE jpa_users SET updated_timestamp = now() WHERE id = ?")) {

                    preparedStatement.setObject(1, user.getId());

                    TimeUnit.SECONDS.sleep(3);

                    preparedStatement.executeUpdate();

                    TimeUnit.SECONDS.sleep(2);

                    connection.releaseSavepoint(savepoint);
                    connection.commit();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                connection.setAutoCommit(true);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                countDownLatch.countDown();
                logger.debug("*********************************** finished blocking update ***********************************");
            }
        };
        */

        Runnable block = () -> {
            logger.debug("*********************************** starting blocking ***********************************");

            try {
                user.setUpdatedTimestamp(ZonedDateTime.now());
                userService.forceRetry(user, 1, 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                countDownLatch.countDown();
                logger.debug("*********************************** finished blocking ***********************************");
            }
        };

        Runnable update = () -> {

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.debug("*********************************** starting retryable ***********************************");

            try {
                user.setUpdatedTimestamp(ZonedDateTime.now());
                userService.save(user);
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
