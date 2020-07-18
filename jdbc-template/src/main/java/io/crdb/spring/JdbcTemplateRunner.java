package io.crdb.spring;

import io.crdb.spring.common.UserDTO;
import io.crdb.spring.common.UserDTOBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcTemplateRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateRunner.class);

    private final UserDTOBuilder userDTOBuilder;
    private final UserService userService;

    public JdbcTemplateRunner(UserDTOBuilder userDTOBuilder, UserService userService) {
        this.userDTOBuilder = userDTOBuilder;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {

        logger.debug("***************************************************** Starting Insert *****************************************************");

        userService.insertUsers(userDTOBuilder.buildUsers());

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

}

