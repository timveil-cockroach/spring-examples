package io.crdb.spring;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionTemplate transactionTemplate;
    private final Faker faker;

    public JpaRunner(UserRepository userRepository, TransactionTemplate transactionTemplate, Faker faker) {
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
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
        userRepository.findByUpdatedTimestampIsNull();
    }

    private void updateUsers() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                userRepository.updateTimestamp(ZonedDateTime.now(EST));
            }
        });
    }

    private void deleteUsers() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                userRepository.deleteAll();
            }
        });
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

