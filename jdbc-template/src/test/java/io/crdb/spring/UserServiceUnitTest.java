package io.crdb.spring;

import io.crdb.spring.common.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private UserService userService;
    private UserDTO testUser;
    private List<UserDTO> testUsers;

    @BeforeEach
    void setUp() {
        userService = new UserService(jdbcTemplate);
        ReflectionTestUtils.setField(userService, "batchSize", 100);
        
        testUser = createTestUser();
        testUsers = Arrays.asList(testUser, createTestUser(), createTestUser());
    }

    @Test
    @DisplayName("Should insert single user successfully")
    void shouldInsertUserSuccessfully() {
        when(jdbcTemplate.update(anyString(), any(org.springframework.jdbc.core.PreparedStatementSetter.class))).thenReturn(1);

        assertDoesNotThrow(() -> userService.insertUser(testUser));

        verify(jdbcTemplate).update(eq("INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)"), any(org.springframework.jdbc.core.PreparedStatementSetter.class));
    }

    @Test
    @DisplayName("Should insert multiple users using batch update")
    void shouldInsertUsersUsingBatchUpdate() {
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any(ParameterizedPreparedStatementSetter.class)))
            .thenReturn(new int[][]{new int[]{1, 1, 1}});

        assertDoesNotThrow(() -> userService.insertUsers(testUsers));

        verify(jdbcTemplate).batchUpdate(
            eq("INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)"),
            eq(testUsers),
            eq(100),
            any(ParameterizedPreparedStatementSetter.class)
        );
    }

    @Test
    @DisplayName("Should select users with null updated_timestamp")
    void shouldSelectUsersWithNullUpdatedTimestamp() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(testUsers);

        List<UserDTO> result = userService.selectUsers();

        assertEquals(3, result.size());
        verify(jdbcTemplate).query(
            eq("SELECT * FROM jdbc_template_users WHERE updated_timestamp IS NULL"),
            any(RowMapper.class)
        );
    }

    @Test
    @DisplayName("Should select single user by ID")
    void shouldSelectSingleUserById() {
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyString()))
            .thenReturn(testUser);

        UserDTO result = userService.selectUser(testUser.id());

        assertEquals(testUser.id(), result.id());
        verify(jdbcTemplate).queryForObject(
            eq("SELECT * FROM jdbc_template_users WHERE id = ?"),
            any(RowMapper.class),
            eq(testUser.id().toString())
        );
    }

    @Test
    @DisplayName("Should update users with null updated_timestamp")
    void shouldUpdateUsersWithNullUpdatedTimestamp() {
        when(jdbcTemplate.update(anyString(), any(Timestamp.class))).thenReturn(5);

        int result = userService.updateUsers();

        assertEquals(5, result);
        verify(jdbcTemplate).update(
            eq("UPDATE jdbc_template_users SET updated_timestamp = ? WHERE updated_timestamp IS NULL"),
            any(Timestamp.class)
        );
    }

    @Test
    @DisplayName("Should update single user by ID")
    void shouldUpdateSingleUserById() {
        when(jdbcTemplate.update(anyString(), any(Timestamp.class), anyString())).thenReturn(1);

        int result = userService.updateUser(testUser.id());

        assertEquals(1, result);
        verify(jdbcTemplate).update(
            eq("UPDATE jdbc_template_users SET updated_timestamp = ? WHERE id = ?"),
            any(Timestamp.class),
            eq(testUser.id().toString())
        );
    }

    @Test
    @DisplayName("Should delete users with non-null updated_timestamp")
    void shouldDeleteUsersWithNonNullUpdatedTimestamp() {
        when(jdbcTemplate.update(anyString())).thenReturn(3);

        int result = userService.deleteUsers();

        assertEquals(3, result);
        verify(jdbcTemplate).update("DELETE FROM jdbc_template_users WHERE updated_timestamp IS NOT NULL");
    }

    @Test
    @DisplayName("Should truncate users table")
    void shouldTruncateUsersTable() {
        when(jdbcTemplate.update(anyString())).thenReturn(0);

        int result = userService.truncate();

        assertEquals(0, result);
        verify(jdbcTemplate).update("TRUNCATE TABLE jdbc_template_users");
    }

    @Test
    @DisplayName("Should execute blocker method with runnable")
    void shouldExecuteBlockerMethodWithRunnable() {
        when(jdbcTemplate.update(anyString(), any(Timestamp.class), anyString())).thenReturn(1);
        Runnable mockRunnable = mock(Runnable.class);

        assertDoesNotThrow(() -> userService.blocker(testUser.id(), mockRunnable));

        verify(jdbcTemplate).update(
            eq("UPDATE jdbc_template_users SET updated_timestamp = ? WHERE id = ?"),
            any(Timestamp.class),
            eq(testUser.id().toString())
        );
        verify(mockRunnable).run();
    }

    @Test
    @DisplayName("Should handle empty user list for batch insert")
    void shouldHandleEmptyUserListForBatchInsert() {
        List<UserDTO> emptyList = Arrays.asList();
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any(ParameterizedPreparedStatementSetter.class)))
            .thenReturn(new int[][]{});

        assertDoesNotThrow(() -> userService.insertUsers(emptyList));

        verify(jdbcTemplate).batchUpdate(
            eq("INSERT INTO jdbc_template_users VALUES (?,?,?,?,?,?,?,?,?,?)"),
            eq(emptyList),
            eq(100),
            any(ParameterizedPreparedStatementSetter.class)
        );
    }

    private UserDTO createTestUser() {
        return new UserDTO(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@example.com",
            "123 Main St",
            "Anytown",
            "NY",
            "12345",
            ZonedDateTime.now(),
            null
        );
    }
}