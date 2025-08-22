package io.crdb.spring.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostgresRetryClassifierTest {

    @Mock
    private ExceptionChecker exceptionChecker;

    private PostgresRetryClassifier classifier;
    private RetryPolicy alwaysRetryPolicy;
    private RetryPolicy neverRetryPolicy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        alwaysRetryPolicy = new AlwaysRetryPolicy();
        neverRetryPolicy = new NeverRetryPolicy();
        classifier = new PostgresRetryClassifier(exceptionChecker);
    }

    @Test
    @DisplayName("Should return SimpleRetryPolicy for retryable exception")
    void shouldReturnSimpleRetryPolicyForRetryableException() {
        SQLException retryableException = new SQLException("Serialization failure", "40001");
        when(exceptionChecker.shouldRetry(retryableException)).thenReturn(true);

        RetryPolicy result = classifier.classify(retryableException);

        assertTrue(result instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        verify(exceptionChecker).shouldRetry(retryableException);
    }

    @Test
    @DisplayName("Should return NeverRetryPolicy for non-retryable exception")
    void shouldReturnNeverRetryPolicyForNonRetryableException() {
        SQLException nonRetryableException = new SQLException("Constraint violation", "23505");
        when(exceptionChecker.shouldRetry(nonRetryableException)).thenReturn(false);

        RetryPolicy result = classifier.classify(nonRetryableException);

        assertTrue(result instanceof org.springframework.retry.policy.NeverRetryPolicy);
        verify(exceptionChecker).shouldRetry(nonRetryableException);
    }

    @Test
    @DisplayName("Should handle null exception")
    void shouldHandleNullException() {
        when(exceptionChecker.shouldRetry(null)).thenReturn(false);

        RetryPolicy result = classifier.classify(null);

        assertTrue(result instanceof org.springframework.retry.policy.NeverRetryPolicy);
        verify(exceptionChecker).shouldRetry(null);
    }

    @Test
    @DisplayName("Should handle non-SQLException")
    void shouldHandleNonSQLException() {
        RuntimeException runtimeException = new RuntimeException("Non-SQL exception");
        when(exceptionChecker.shouldRetry(runtimeException)).thenReturn(false);

        RetryPolicy result = classifier.classify(runtimeException);

        assertTrue(result instanceof org.springframework.retry.policy.NeverRetryPolicy);
        verify(exceptionChecker).shouldRetry(runtimeException);
    }

    @Test
    @DisplayName("Should handle nested exception with retryable cause")
    void shouldHandleNestedExceptionWithRetryableCause() {
        SQLException sqlException = new SQLException("Serialization failure", "40001");
        RuntimeException wrappedException = new RuntimeException("Wrapped exception", sqlException);
        when(exceptionChecker.shouldRetry(wrappedException)).thenReturn(true);

        RetryPolicy result = classifier.classify(wrappedException);

        assertTrue(result instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        verify(exceptionChecker).shouldRetry(wrappedException);
    }

    @Test
    @DisplayName("Should delegate all exception checking to ExceptionChecker")
    void shouldDelegateAllExceptionCheckingToExceptionChecker() {
        SQLException exception1 = new SQLException("Test 1", "40001");
        SQLException exception2 = new SQLException("Test 2", "40003");
        SQLException exception3 = new SQLException("Test 3", "23505");

        when(exceptionChecker.shouldRetry(exception1)).thenReturn(true);
        when(exceptionChecker.shouldRetry(exception2)).thenReturn(true);
        when(exceptionChecker.shouldRetry(exception3)).thenReturn(false);

        RetryPolicy result1 = classifier.classify(exception1);
        RetryPolicy result2 = classifier.classify(exception2);
        RetryPolicy result3 = classifier.classify(exception3);

        assertTrue(result1 instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        assertTrue(result2 instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        assertTrue(result3 instanceof org.springframework.retry.policy.NeverRetryPolicy);

        verify(exceptionChecker).shouldRetry(exception1);
        verify(exceptionChecker).shouldRetry(exception2);
        verify(exceptionChecker).shouldRetry(exception3);
    }

    @Test
    @DisplayName("Should maintain consistent behavior across multiple calls")
    void shouldMaintainConsistentBehaviorAcrossMultipleCalls() {
        SQLException retryableException = new SQLException("Retry me", "40001");
        when(exceptionChecker.shouldRetry(retryableException)).thenReturn(true);

        RetryPolicy firstCall = classifier.classify(retryableException);
        RetryPolicy secondCall = classifier.classify(retryableException);
        RetryPolicy thirdCall = classifier.classify(retryableException);

        assertTrue(firstCall instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        assertTrue(secondCall instanceof org.springframework.retry.policy.SimpleRetryPolicy);
        assertTrue(thirdCall instanceof org.springframework.retry.policy.SimpleRetryPolicy);

        verify(exceptionChecker, times(3)).shouldRetry(retryableException);
    }
}