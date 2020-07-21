package io.crdb.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    private final UserService userService;
    private final UserBuilder userBuilder;

    @Autowired
    public UserServiceTest(UserService userService, UserBuilder userBuilder) {
        this.userService = userService;
        this.userBuilder = userBuilder;
    }

    private List<User> userList = null;
    private User user = null;
    private Iterable<User> userIterable = null;
    private Iterable<UUID> uuidIterable = null;

    @BeforeEach
    void setUp() {
        userList = userBuilder.buildUsers(10);
        user = userList.get(0);
        userIterable = () -> userList.iterator();

        List<UUID> ids = new ArrayList<>();

        for (User user : userList) {
            ids.add(user.getId());
        }

        uuidIterable = () -> ids.iterator();
    }

    @AfterEach
    void tearDown() {

        logger.debug("*********************************** starting tearDown ***********************************");

        this.userList = null;
        this.user = null;
        this.userIterable = null;
        this.uuidIterable = null;

        try {
            userService.deleteAll();
        } catch (EmptyResultDataAccessException ignored) {

        }

        logger.debug("*********************************** finished tearDown ***********************************");
    }

    @Test
    void saveAll() {
        logger.debug("*********************************** starting saveAll ***********************************");
        Iterable<User> users = userService.saveAll(userList);
        logger.debug("*********************************** finished saveAll ***********************************");
    }

    @Test
    void testSaveAll() {
        logger.debug("*********************************** starting testSaveAll ***********************************");
        Iterable<User> users = userService.saveAll(userIterable);
        logger.debug("*********************************** finished testSaveAll ***********************************");
    }

    @Test
    void save() {
        logger.debug("*********************************** starting save ***********************************");
        User save = userService.save(user);
        logger.debug("*********************************** finished save ***********************************");
    }

    @Test
    void findAll() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting findAll ***********************************");
        Iterable<User> all = userService.findAll();
        logger.debug("*********************************** finished findAll ***********************************");
    }

    @Test
    void testFindAll() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting testFindAll ***********************************");
        Iterable<User> all = userService.findAll(uuidIterable);
        logger.debug("*********************************** finished testFindAll ***********************************");
    }

    @Test
    void find() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting find ***********************************");
        Optional<User> user = userService.find(this.user.getId());
        logger.debug("*********************************** finished find ***********************************");
    }

    @Test
    void exists() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting exists ***********************************");
        boolean exists = userService.exists(user.getId());
        logger.debug("*********************************** finished exists ***********************************");
    }

    @Test
    void count() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting count ***********************************");
        long count = userService.count();
        logger.debug("*********************************** finished count ***********************************");
    }

    @Test
    void deleteAll() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting deleteAll ***********************************");
        userService.deleteAll();
        logger.debug("*********************************** finished deleteAll ***********************************");
    }

    @Test
    void testDeleteAll() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting testDeleteAll ***********************************");
        userService.deleteAll(userIterable);
        logger.debug("*********************************** finished testDeleteAll ***********************************");
    }

    @Test
    void delete() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting delete ***********************************");
        userService.delete(user.getId());
        logger.debug("*********************************** finished delete ***********************************");
    }

    @Test
    void testDelete() {
        userService.saveAll(userIterable);

        logger.debug("*********************************** starting testDelete ***********************************");
        userService.delete(user);
        logger.debug("*********************************** finished testDelete ***********************************");
    }
}