package io.crdb.spring;

import io.crdb.spring.common.UserDTO;
import io.crdb.spring.common.UserDTOBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    private final UserService userService;
    private final UserDTOBuilder userBuilder;

    @Autowired
    public UserServiceTest(UserService userService, UserDTOBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    private List<UserDTO> userList = null;
    private UserDTO user = null;

    @BeforeEach
    void setUp() {
        userList = userBuilder.buildUsers(10);
        user = userList.get(0);

    }

    @AfterEach
    void tearDown() {

        logger.debug("*********************************** starting tearDown ***********************************");

        this.userList = null;
        this.user = null;

        userService.truncate();

        logger.debug("*********************************** finished tearDown ***********************************");
    }

    @Test
    void insertUsers() {
        logger.debug("*********************************** starting insertUsers ***********************************");
        userService.insertUsers(userList);
        logger.debug("*********************************** finished insertUsers ***********************************");
    }

    @Test
    void insertUser() {
        logger.debug("*********************************** starting insertUser ***********************************");
        userService.insertUser(user);
        logger.debug("*********************************** finished insertUser ***********************************");
    }

    @Test
    void selectUsers() {
        userService.insertUsers(userList);

        logger.debug("*********************************** starting selectUsers ***********************************");
        List<UserDTO> all = userService.selectUsers();
        logger.debug("*********************************** finished selectUsers ***********************************");
    }

    @Test
    void updateUsers() {
        userService.insertUsers(userList);

        logger.debug("*********************************** starting updateUsers ***********************************");
        int updateUsers = userService.updateUsers();
        logger.debug("*********************************** finished updateUsers ***********************************");
    }

    @Test
    void updateUser() {
        userService.insertUser(user);

        logger.debug("*********************************** starting updateUser ***********************************");
        int updateUsers = userService.updateUser(user.getId());

        logger.debug("*********************************** finished updateUser ***********************************");
    }

    @Test
    void deleteUsers() {
        userService.insertUsers(userList);

        logger.debug("*********************************** starting deleteUsers ***********************************");
        int deleteUsers = userService.deleteUsers();
        logger.debug("*********************************** finished deleteUsers ***********************************");
    }

}