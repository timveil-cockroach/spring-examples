package io.crdb.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@Profile("!test")
public class JpaCRUDRunner implements ApplicationRunner, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JpaCRUDRunner.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    public JpaCRUDRunner(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    @Override
    public void run(ApplicationArguments args) {

        User newUser = userBuilder.buildUser();

        logger.debug("***************************************************** Starting Save *****************************************************");

        User savedUser = userService.save(newUser);

        assert newUser.getId().equals(savedUser.getId());

        logger.debug("***************************************************** Starting Find User *****************************************************");

        Optional<User> optionalUser = userService.find(savedUser.getId());

        assert optionalUser.isPresent();

        User foundUser = optionalUser.get();

        assert savedUser.getId().equals(foundUser.getId());

        logger.debug("***************************************************** Starting Update User *****************************************************");

        ZonedDateTime now = ZonedDateTime.now();

        foundUser.setUpdatedTimestamp(now);

        User updatedUser = userService.save(foundUser);

        assert updatedUser.getId().equals(foundUser.getId());

        assert now.equals(updatedUser.getUpdatedTimestamp());

        logger.debug("***************************************************** Starting Delete User *****************************************************");

        userService.delete(updatedUser.getId());

        logger.debug("***************************************************** Starting Exists User *****************************************************");

        boolean exists = userService.exists(updatedUser.getId());

        assert !exists;

        logger.debug("***************************************************** Starting Count Users *****************************************************");

        long finalCount = userService.count();

        assert finalCount == 0;

        logger.debug("***************************************************** Exiting JpaCRUDRunner *****************************************************");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

