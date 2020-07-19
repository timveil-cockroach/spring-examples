package io.crdb.spring;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Profile("!test")
public class JpaBatchRunner implements ApplicationRunner, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JpaBatchRunner.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    public JpaBatchRunner(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    @Override
    public void run(ApplicationArguments args) {

        List<User> newUsers = userBuilder.buildUsers();
        int newUsersSize = newUsers.size();

        logger.debug("working with {} users", newUsersSize);

        logger.debug("***************************************************** Starting Save All *****************************************************");

        Iterable<User> savedUsers = userService.saveAll(newUsers);

        int savedUsersSize = Iterables.size(savedUsers);

        assert newUsersSize == savedUsersSize;

        logger.debug("***************************************************** Starting Find Users *****************************************************");

        Iterable<User> foundUsers = userService.findAll();

        int foundUsersSize = Iterables.size(foundUsers);

        assert foundUsersSize == savedUsersSize;

        logger.debug("***************************************************** Starting Update Users *****************************************************");

        for (User user : foundUsers) {
            user.setUpdatedTimestamp(ZonedDateTime.now());
        }

        Iterable<User> updatedUsers = userService.saveAll(foundUsers);

        assert foundUsersSize == Iterables.size(updatedUsers);

        logger.debug("***************************************************** Starting Delete Users *****************************************************");

        userService.deleteAll(updatedUsers);

        logger.debug("***************************************************** Starting Count Users *****************************************************");

        long finalCount = userService.count();

        assert finalCount == 0;

        logger.debug("***************************************************** Exiting JpaBatchRunner *****************************************************");
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

