package io.crdb.spring.datasource;

import io.crdb.spring.UserService;
import io.crdb.spring.common.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private RetryTemplate retryTemplate;

    private UserService userService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        
        when(dataSource.getConnection()).thenReturn(connection);
        
        userService = new UserService(dataSource, retryTemplate);
        
        // Set batchSize to avoid division by zero
        ReflectionTestUtils.setField(userService, "batchSize", 1000);
    }

    @Test
    @DisplayName("Should insert users successfully")
    void shouldInsertUsersSuccessfully() throws SQLException {
        List<UserDTO> users = createTestUsers(3);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1, 1, 1});
        
        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });

        userService.insertUsers(users);

        verify(connection).prepareStatement(contains("INSERT INTO datasource_users"));
        verify(preparedStatement, times(3)).setString(eq(1), anyString());
        verify(preparedStatement, times(3)).setString(eq(2), anyString());
        verify(preparedStatement, times(3)).setString(eq(3), anyString());
        verify(preparedStatement, times(3)).addBatch();
        verify(preparedStatement, atLeastOnce()).executeBatch();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should handle SQLException during insert")
    void shouldHandleSQLExceptionDuringInsert() throws Exception {
        List<UserDTO> users = createTestUsers(1);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        
        SQLException sqlException = new SQLException("Connection failed", "08003");
        when(preparedStatement.executeBatch()).thenThrow(sqlException);
        
        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });

        assertThrows(SQLException.class, () -> userService.insertUsers(users));
        
        verify(connection).close();
    }

    @Test
    @DisplayName("Should select users and map results correctly")
    void shouldSelectUsersAndMapResultsCorrectly() throws SQLException {
        setupResultSetForSelectUsers();
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<UserDTO> result = userService.selectUsers();

        assertEquals(2, result.size());
        
        UserDTO firstUser = result.get(0);
        assertEquals("00000000-0000-0000-0000-000000000001", firstUser.id().toString());
        assertEquals("John", firstUser.firstName());
        assertEquals("Doe", firstUser.lastName());
        assertEquals("New York", firstUser.city());
        assertEquals("NY", firstUser.stateCode());
        assertEquals("10001", firstUser.zipCode());

        verify(connection).prepareStatement(contains("SELECT * FROM datasource_users"));
        verify(preparedStatement).executeQuery();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should update users successfully")
    void shouldUpdateUsersSuccessfully() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(5);
        
        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });

        int result = userService.updateUsers();

        assertEquals(5, result);
        verify(connection).prepareStatement(contains("UPDATE datasource_users SET updated_timestamp"));
        verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should delete users successfully")
    void shouldDeleteUsersSuccessfully() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(3);
        
        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(mock(RetryContext.class));
        });

        int result = userService.deleteUsers();

        assertEquals(3, result);
        verify(connection).prepareStatement(contains("DELETE FROM datasource_users WHERE updated_timestamp IS NOT NULL"));
        verify(preparedStatement).executeUpdate();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should handle connection failures gracefully")
    void shouldHandleConnectionFailuresGracefully() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection pool exhausted"));

        assertThrows(SQLException.class, () -> userService.selectUsers());
        
        verify(dataSource).getConnection();
    }

    private List<UserDTO> createTestUsers(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new UserDTO(
                        UUID.randomUUID(),
                        "First" + i,
                        "Last" + i,
                        "email" + i + "@example.com",
                        "123 Test St " + i,
                        "City" + i,
                        "ST",
                        "1000" + i,
                        ZonedDateTime.now(),
                        null
                ))
                .toList();
    }

    private void setupResultSetForSelectUsers() throws SQLException {
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString(eq("id")))
                .thenReturn("00000000-0000-0000-0000-000000000001")
                .thenReturn("00000000-0000-0000-0000-000000000002");

        when(resultSet.getString("first_name"))
                .thenReturn("John")
                .thenReturn("Jane");

        when(resultSet.getString("last_name"))
                .thenReturn("Doe")
                .thenReturn("Smith");

        when(resultSet.getString("email"))
                .thenReturn("john@example.com")
                .thenReturn("jane@example.com");

        when(resultSet.getString("address"))
                .thenReturn("123 Main St")
                .thenReturn("456 Oak Ave");

        when(resultSet.getString("city"))
                .thenReturn("New York")
                .thenReturn("Los Angeles");

        when(resultSet.getString("state_code"))
                .thenReturn("NY")
                .thenReturn("CA");

        when(resultSet.getString("zip_code"))
                .thenReturn("10001")
                .thenReturn("90001");

        when(resultSet.getTimestamp("created_timestamp"))
                .thenReturn(new Timestamp(System.currentTimeMillis()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));

        when(resultSet.getTimestamp("updated_timestamp"))
                .thenReturn(null)
                .thenReturn(null);
    }
}