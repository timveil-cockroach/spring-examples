package io.crdb.spring;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JpaRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(JpaRunner.class);


    @Value("${datasource.row.size}")
    private int rowSize;

    private final UserService userService;
    private final Faker faker;

    public JpaRunner(UserService userService, Faker faker) {
        this.userService = userService;
        this.faker = faker;
    }

    @Override
    public void run(ApplicationArguments args) {

        logger.debug("***************************************************** Starting Insert *****************************************************");

        Iterable<User> newUsers = userService.insertUsers(buildUsers());

        for (User user : newUsers) {
            logger.trace("created user {}", user.toString());
        }

        logger.debug("***************************************************** Starting Select All *****************************************************");

        List<User> users = userService.selectUsers();

        logger.debug("selected {} users", users.size());

        logger.debug("***************************************************** Starting Update *****************************************************");

        int updateUsers = userService.updateUsers();

        logger.debug("updated {} users", updateUsers);

        assert users.size() != updateUsers;

        logger.debug("***************************************************** Starting Delete *****************************************************");

        userService.deleteUsers();

        logger.debug("deleted all users");

        logger.debug("***************************************************** Starting Count *****************************************************");

        long finalCount = userService.countUsers();

        logger.debug("found {} users", finalCount);

        assert finalCount == 0;

        logger.debug("***************************************************** Exiting *****************************************************");
    }

    private List<User> buildUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            users.add(new User(
                    UUID.randomUUID(),
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.internet().safeEmailAddress(),
                    faker.address().streetAddress(),
                    faker.address().city(),
                    faker.address().stateAbbr(),
                    faker.address().zipCode(),
                    ZonedDateTime.now(),
                    null
            ));
        }
        return users;
    }
}

