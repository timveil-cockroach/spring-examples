package io.crdb.spring.common;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class UserDTOBuilder {

    @Value("${demo.row.size}")
    private int rowSize;

    private final Faker faker;

    public UserDTOBuilder(Faker faker) {
        this.faker = faker;
    }

    public List<UserDTO> buildUsers() {
        return buildUsers(rowSize);
    }

    public List<UserDTO> buildUsers(int num) {
        List<UserDTO> users = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            users.add(buildUser());
        }
        return users;
    }

    public UserDTO buildUser() {
        return new UserDTO(
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
