package io.crdb.spring;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class DatasourceRunner implements ApplicationRunner {

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

        insertData();

        // select

        // update
    }

    private void insertData() throws SQLException {

        String sql = "INSERT INTO users VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < rowSize; i++) {

                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, faker.name().firstName());
                ps.setString(3, faker.name().lastName());
                ps.setString(4, faker.internet().safeEmailAddress());
                ps.setString(5, faker.address().streetAddress());
                ps.setString(6, faker.address().city());
                ps.setString(7, faker.address().stateAbbr());
                ps.setString(8, faker.address().zipCode());
                ps.setTimestamp(9, Timestamp.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
                ps.setTimestamp(10, null);
                ps.addBatch();

                if (i % batchSize == 0) {
                    ps.executeBatch();
                }

            }

            ps.executeBatch();
        }
    }
}
