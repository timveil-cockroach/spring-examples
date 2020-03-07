package io.crdb.spring;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class DatasourceRunner implements ApplicationRunner {

    private static final ZoneId EST = ZoneId.of("America/New_York");
    private static final Logger logger = LoggerFactory.getLogger(DatasourceRunner.class);

    @Value("${datasource.batch.size}")
    private int batchSize;

    @Value("${datasource.row.size}")
    private int rowSize;

    private final DataSource dataSource;
    private final Faker faker;

    public DatasourceRunner(DataSource dataSource, Faker faker) {
        this.dataSource = dataSource;
        this.faker = faker;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        insertUsers();

        selectUsers();

        updateUsers();

        deleteUsers();

    }

    private void insertUsers() throws SQLException {

        String sql = "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = 0;
            for (int i = 0; i < rowSize; i++) {

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, faker.name().firstName());
                ps.setString(3, faker.name().lastName());
                ps.setString(4, faker.internet().safeEmailAddress());
                ps.setString(5, faker.address().streetAddress());
                ps.setString(6, faker.address().city());
                ps.setString(7, faker.address().stateAbbr());
                ps.setString(8, faker.address().zipCode());
                ps.setTimestamp(9, Timestamp.from(ZonedDateTime.now(EST).toInstant()));
                ps.setTimestamp(10, null);

                ps.addBatch();

                if (++count % batchSize == 0) {
                    int[] batch = ps.executeBatch();

                    logger.debug("inserted {} users", batch.length);
                }

            }

            int[] remainingBatch = ps.executeBatch();

            logger.debug("inserted remaining {} users", remainingBatch.length);
        }
    }

    private void selectUsers() throws SQLException {

        String sql = "SELECT * FROM users WHERE updated_timestamp IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs != null) {

                int count = 0;

                while (rs.next()) {

                    UserDTO user = new UserDTO(
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
                    );

                    if (++count % 500 == 0) {
                        logger.debug("sample user [{}]", user.toString());
                    }


                }
            }

        }

    }

    private void updateUsers() throws SQLException {

        String sql = "UPDATE users SET updated_timestamp = ? WHERE updated_timestamp IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(ZonedDateTime.now(EST).toInstant()));

            int usersUpdated = ps.executeUpdate();

            logger.debug("updated {} users", usersUpdated);
        }

    }

    private void deleteUsers() throws SQLException {

        String sql = "DELETE FROM users WHERE updated_timestamp IS NOT NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int usersDeleted = ps.executeUpdate();

            logger.debug("deleted {} users", usersDeleted);
        }

    }

    private ZonedDateTime fromTimestamp(java.sql.Timestamp timestamp) {

        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), EST);

    }
}

