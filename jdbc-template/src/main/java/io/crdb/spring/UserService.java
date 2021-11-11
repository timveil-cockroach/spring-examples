package io.crdb.spring;

import io.crdb.spring.common.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private static final String INSERT_SQL = "INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)";
    public static final String SELECT_SQL = "SELECT * FROM jdbc_template_users WHERE id = ?";
    public static final String UPDATE_SQL = "UPDATE jdbc_template_users SET updated_timestamp = ? WHERE id = ?";

    @Value("${demo.batch.size}")
    private int batchSize;

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public void insertUsers(List<UserDTO> users) {
        jdbcTemplate.batchUpdate(INSERT_SQL, users, batchSize, this::mapUserToStatement);
    }


    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public void insertUser(UserDTO user) {
        jdbcTemplate.update(INSERT_SQL, ps -> mapUserToStatement(ps, user)
        );
    }


    @Transactional(readOnly = true)
    public List<UserDTO> selectUsers() {
        final String sql = "SELECT * FROM jdbc_template_users WHERE updated_timestamp IS NULL";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> getUserFromResultSet(rs)
        );
    }

    @Transactional(readOnly = true)
    public UserDTO selectUser(UUID id) {
        return jdbcTemplate.queryForObject(SELECT_SQL,
                (rs, rowNum) -> getUserFromResultSet(rs),
                id.toString()
        );
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public int updateUsers() {
        final String sql = "UPDATE jdbc_template_users SET updated_timestamp = ? WHERE updated_timestamp IS NULL";

        return jdbcTemplate.update(sql, Timestamp.from(ZonedDateTime.now().toInstant()));
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public int updateUser(UUID id) {
        return jdbcTemplate.update(UPDATE_SQL, Timestamp.from(ZonedDateTime.now().toInstant()), id.toString());
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public int deleteUsers() {
        final String sql = "DELETE FROM jdbc_template_users WHERE updated_timestamp IS NOT NULL";

        return jdbcTemplate.update(sql);
    }

    @Transactional
    @Retryable(exceptionExpression = "@exceptionChecker.shouldRetry(#root)")
    public int truncate() {
        final String sql = "TRUNCATE TABLE jdbc_template_users";

        return jdbcTemplate.update(sql);
    }

    @Transactional
    public void blocker(UUID id, Runnable runnable) {
        jdbcTemplate.update(UPDATE_SQL, Timestamp.from(ZonedDateTime.now().toInstant()), id.toString());
        runnable.run();
    }

    private void mapUserToStatement(PreparedStatement ps, UserDTO user) throws SQLException {
        ps.setString(1, user.id().toString());
        ps.setString(2, user.firstName());
        ps.setString(3, user.lastName());
        ps.setString(4, user.email());
        ps.setString(5, user.address());
        ps.setString(6, user.city());
        ps.setString(7, user.stateCode());
        ps.setString(8, user.zipCode());
        ps.setTimestamp(9, Timestamp.from(user.createdTimestamp().toInstant()));
        ps.setTimestamp(10, null);
    }

    private UserDTO getUserFromResultSet(ResultSet rs) throws SQLException {
        return new UserDTO(
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
    }


    private ZonedDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
    }
}
