package io.crdb.spring.common;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserBuilder {

    @Value("${datasource.row.size}")
    private int rowSize;

    private final Faker faker;

    public UserBuilder(Faker faker) {
        this.faker = faker;
    }

    public List<UserDTO> buildUsers() {
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
