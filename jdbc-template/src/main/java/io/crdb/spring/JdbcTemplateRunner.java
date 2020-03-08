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
public class JdbcTemplateRunner implements ApplicationRunner {

    private static final ZoneId EST = ZoneId.of("America/New_York");
    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplateRunner.class);

    @Value("${datasource.batch.size}")
    private int batchSize;

    @Value("${datasource.row.size}")
    private int rowSize;

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker;

    public JdbcTemplateRunner(JdbcTemplate jdbcTemplate, Faker faker) {
        this.jdbcTemplate = jdbcTemplate;
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
        final String sql = "INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)";

        jdbcTemplate.batchUpdate(
                sql,
                buildUsers(),
                batchSize,
                (ps, argument) -> {
                    ps.setString(1, argument.getId().toString());
                    ps.setString(2, argument.getFirstName());
                    ps.setString(3, argument.getLastName());
                    ps.setString(4, argument.getEmail());
                    ps.setString(5, argument.getAddress());
                    ps.setString(6, argument.getCity());
                    ps.setString(7, argument.getStateCode());
                    ps.setString(8, argument.getZipCode());
                    ps.setTimestamp(9, Timestamp.from(argument.getCreatedTimestamp().toInstant()));
                    ps.setTimestamp(10, null);
                });
    }


    private void selectUsers() {
        final String sql = "SELECT * FROM jdbc_template_users WHERE updated_timestamp IS NULL";

        jdbcTemplate.query(sql, (rs, rowNum) -> new UserDTO(
                UUID.fromString(rs.getString("id")),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("state_code"),
                rs.getString("zip_code"),
                fromTimestamp(rs.getTimestamp("created_timestamp")),
                fromTimestamp(rs.getTimestamp("updated_timestamp"))
        ));
    }

    private void updateUsers() {
        final String sql = "UPDATE jdbc_template_users SET updated_timestamp = ? WHERE updated_timestamp IS NULL";

        jdbcTemplate.update(sql, Timestamp.from(ZonedDateTime.now(EST).toInstant()));
    }

    private void deleteUsers() {
        final String sql = "DELETE FROM jdbc_template_users WHERE updated_timestamp IS NOT NULL";

        jdbcTemplate.update(sql);
    }

    private ZonedDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), EST);
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
                    ZonedDateTime.now(EST),
                    null
            ));
        }
        return users;
    }
}

