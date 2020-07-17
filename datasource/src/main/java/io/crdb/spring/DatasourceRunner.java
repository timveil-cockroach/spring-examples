package io.crdb.spring;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DatasourceRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatasourceRunner.class);

    @Value("${datasource.row.size}")
    private int rowSize;

    private final Faker faker;
    private final UserService userService;

    public DatasourceRunner(Faker faker, UserService userService) {
        this.faker = faker;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        logger.debug("***************************************************** Starting Insert *****************************************************");

        userService.insertUsers(buildUsers());

        logger.debug("***************************************************** Starting Select All *****************************************************");

        List<UserDTO> users = userService.selectUsers();

        logger.debug("selected {} users", users.size());

        logger.debug("***************************************************** Starting Update *****************************************************");

        int updateUsers = userService.updateUsers();

        logger.debug("updated {} users", updateUsers);

        assert users.size() != updateUsers;

        logger.debug("***************************************************** Starting Delete *****************************************************");

        int deletedUsers = userService.deleteUsers();

        logger.debug("deleted {} users", deletedUsers);

        assert users.size() != deletedUsers;

        logger.debug("***************************************************** Exiting *****************************************************");

    }


    private List<UserDTO> buildUsers() {
        List<UserDTO> users = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            users.add(new UserDTO(
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

