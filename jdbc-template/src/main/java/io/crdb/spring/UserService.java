package io.crdb.spring;

import io.crdb.spring.common.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Value("${datasource.batch.size}")
    private int batchSize;

    private final JdbcTemplate jdbcTemplate;
    private final RetryTemplate retryTemplate;

    public UserService(JdbcTemplate jdbcTemplate, RetryTemplate retryTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.retryTemplate = retryTemplate;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void insertUsers(List<UserDTO> users) {
        final String sql = "INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)";

        retryTemplate.execute(
                context -> jdbcTemplate.batchUpdate(
                        sql,
                        users,
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
                        }
                )
        );
    }


    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = true)
    public List<UserDTO> selectUsers() {
        final String sql = "SELECT * FROM jdbc_template_users WHERE updated_timestamp IS NULL";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new UserDTO(
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
                )
        );
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int updateUsers() {
        final String sql = "UPDATE jdbc_template_users SET updated_timestamp = ? WHERE updated_timestamp IS NULL";

        return retryTemplate.execute(context -> jdbcTemplate.update(sql, Timestamp.from(ZonedDateTime.now().toInstant())));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public int deleteUsers() {
        final String sql = "DELETE FROM jdbc_template_users WHERE updated_timestamp IS NOT NULL";

        return retryTemplate.execute(context -> jdbcTemplate.update(sql));
    }

    private ZonedDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
}
