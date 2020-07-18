package io.crdb.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JpaRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(JpaRunner.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    public JpaRunner(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    @Override
    public void run(ApplicationArguments args) {

        logger.debug("***************************************************** Starting Insert *****************************************************");

        Iterable<User> newUsers = userService.insertUsers(userBuilder.buildUsers());

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

}

