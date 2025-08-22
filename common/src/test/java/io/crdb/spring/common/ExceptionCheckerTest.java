package io.crdb.spring.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionCheckerTest {

    private ExceptionChecker exceptionChecker;

    @BeforeEach
    void setUp() {
        exceptionChecker = new ExceptionChecker();
    }

    @Test
    @DisplayName("Should return false for null exception")
    void shouldReturnFalseForNullException() {
        assertFalse(exceptionChecker.shouldRetry(null));
    }

    @Test
    @DisplayName("Should return false for non-SQLException")
    void shouldReturnFalseForNonSQLException() {
        Throwable nonSQLException = new RuntimeException("Not a SQL exception");
        assertFalse(exceptionChecker.shouldRetry(nonSQLException));
    }

    @Test
    @DisplayName("Should return false for SQLException with null SQLState")
    void shouldReturnFalseForSQLExceptionWithNullState() {
        SQLException sqlException = new SQLException("Test exception", (String) null);
        assertFalse(exceptionChecker.shouldRetry(sqlException));
    }

    @ParameterizedTest
    @ValueSource(strings = {"40001", "40003", "08003", "08006"})
    @DisplayName("Should return true for retryable SQL states")
    void shouldReturnTrueForRetryableSQLStates(String sqlState) {
        SQLException sqlException = new SQLException("Test exception", sqlState);
        assertTrue(exceptionChecker.shouldRetry(sqlException));
    }

    @Test
    @DisplayName("Should return false for non-retryable SQL state")
    void shouldReturnFalseForNonRetryableSQLState() {
        SQLException sqlException = new SQLException("Test exception", "22003");
        assertFalse(exceptionChecker.shouldRetry(sqlException));
    }

    @Test
    @DisplayName("Should handle nested SQLException with retryable state")
    void shouldHandleNestedSQLExceptionWithRetryableState() {
        SQLException innerException = new SQLException("Inner exception", "40001");
        RuntimeException outerException = new RuntimeException("Outer exception", innerException);
        assertTrue(exceptionChecker.shouldRetry(outerException));
    }

    @Test
    @DisplayName("Should handle deeply nested SQLException with retryable state")
    void shouldHandleDeeplyNestedSQLExceptionWithRetryableState() {
        SQLException innerException = new SQLException("Inner exception", "40003");
        RuntimeException middleException = new RuntimeException("Middle exception", innerException);
        Exception outerException = new Exception("Outer exception", middleException);
        assertTrue(exceptionChecker.shouldRetry(outerException));
    }

    @Test
    @DisplayName("Should return false for nested non-retryable SQLException")
    void shouldReturnFalseForNestedNonRetryableSQLException() {
        SQLException innerException = new SQLException("Inner exception", "23505");
        RuntimeException outerException = new RuntimeException("Outer exception", innerException);
        assertFalse(exceptionChecker.shouldRetry(outerException));
    }

    @Test
    @DisplayName("Should only check first SQLException not chain")
    void shouldOnlyCheckFirstSQLExceptionNotChain() {
        SQLException nextException = new SQLException("Next exception", "40001");
        SQLException firstException = new SQLException("First exception", "22003");
        firstException.setNextException(nextException);
        
        // Only checks the first exception, not the chain
        assertFalse(exceptionChecker.shouldRetry(firstException));
    }

    @Test
    @DisplayName("Should return false when all exceptions in chain are non-retryable")
    void shouldReturnFalseWhenAllExceptionsInChainAreNonRetryable() {
        SQLException nextException = new SQLException("Next exception", "23505");
        SQLException firstException = new SQLException("First exception", "22003");
        firstException.setNextException(nextException);
        
        assertFalse(exceptionChecker.shouldRetry(firstException));
    }

    @Test
    @DisplayName("Should find first SQLException in hierarchy")
    void shouldFindFirstSQLExceptionInHierarchy() {
        SQLException sqlException1 = new SQLException("SQL 1", "22003");
        SQLException sqlException2 = new SQLException("SQL 2", "08003");
        sqlException1.setNextException(sqlException2);
        
        RuntimeException wrapper = new RuntimeException("Wrapper", sqlException1);
        // Only checks the first SQLException found, not its next exceptions
        assertFalse(exceptionChecker.shouldRetry(wrapper));
    }

    @Test
    @DisplayName("Should return false for empty SQLState string")
    void shouldReturnFalseForEmptySQLState() {
        SQLException sqlException = new SQLException("Test exception", "");
        assertFalse(exceptionChecker.shouldRetry(sqlException));
    }

    @Test
    @DisplayName("Should handle case sensitivity in SQL state codes")
    void shouldHandleCaseSensitivityInSQLStateCodes() {
        SQLException upperCase = new SQLException("Test", "40001");
        assertTrue(exceptionChecker.shouldRetry(upperCase));
        
        SQLException mixedCase = new SQLException("Test", "40001");
        assertTrue(exceptionChecker.shouldRetry(mixedCase));
    }
}