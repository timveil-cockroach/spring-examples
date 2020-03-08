package io.crdb.spring;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JpaRunner implements ApplicationRunner {

    private static final ZoneId EST = ZoneId.of("America/New_York");
    private static final Logger logger = LoggerFactory.getLogger(JpaRunner.class);


    @Value("${datasource.row.size}")
    private int rowSize;

    private final UserRepository userRepository;
    private final Faker faker;

    public JpaRunner(UserRepository userRepository, Faker faker) {
        this.userRepository = userRepository;
        this.faker = faker;
    }

    @Override
    public void run(ApplicationArguments args) {

        insertUsers();

        selectUsers();

        updateUsers();

        deleteUsers();

    }

    private void insertUsers() {
        userRepository.saveAll(buildUsers());
    }


    private void selectUsers() {

    }

    private void updateUsers() {

    }

    private void deleteUsers() {

    }

    private ZonedDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), EST);
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
                    ZonedDateTime.now(EST),
                    null
            ));

        }
        return users;
    }
}

