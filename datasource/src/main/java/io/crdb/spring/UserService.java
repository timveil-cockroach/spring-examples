package io.crdb.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${datasource.batch.size}")
    private int batchSize;

    private final DataSource dataSource;

    public UserService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void insertUsers(List<UserDTO> users) throws SQLException {
        final String sql = "INSERT INTO datasource_users VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int count = 0;

            for (UserDTO user : users) {

                ps.setString(1, user.getId().toString());
                ps.setString(2, user.getFirstName());
                ps.setString(3, user.getLastName());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getAddress());
                ps.setString(6, user.getCity());
                ps.setString(7, user.getStateCode());
                ps.setString(8, user.getZipCode());
                ps.setTimestamp(9, Timestamp.from(user.getCreatedTimestamp().toInstant()));
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

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public List<UserDTO> selectUsers() throws SQLException {
        List<UserDTO> users = new ArrayList<>();

        final String sql = "SELECT * FROM datasource_users WHERE updated_timestamp IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs != null) {
                while (rs.next()) {
                    users.add(new UserDTO(
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
            }
        }

        return users;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int updateUsers() throws SQLException {
        final String sql = "UPDATE datasource_users SET updated_timestamp = ? WHERE updated_timestamp IS NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.from(ZonedDateTime.now().toInstant()));

            return ps.executeUpdate();
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int deleteUsers() throws SQLException {
        final String sql = "DELETE FROM datasource_users WHERE updated_timestamp IS NOT NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            return ps.executeUpdate();
        }
    }


    private ZonedDateTime fromTimestamp(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
}
