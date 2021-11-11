package io.crdb.spring;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserBuilder {

    @Value("${demo.row.size}")
    private int rowSize;

    private final Faker faker;

    public UserBuilder(Faker faker) {
        this.faker = faker;
    }

    public List<User> buildUsers() {
        return buildUsers(rowSize);
    }

    public List<User> buildUsers(int num) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            users.add(buildUser());
        }
        return users;
    }

    public User buildUser() {
        return new User(
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
        );
    }
}
