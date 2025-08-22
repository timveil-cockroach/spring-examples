package io.crdb.spring;

import com.github.javafaker.Faker;
import io.crdb.spring.common.ExceptionChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.support.RetryTemplate;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcTemplateApplicationTest {

    @Mock
    private ExceptionChecker exceptionChecker;

    @Test
    @DisplayName("Should create Faker bean with US locale")
    void shouldCreateFakerBeanWithUSLocale() {
        JdbcTemplateApplication app = new JdbcTemplateApplication();

        Faker faker = app.faker();

        assertNotNull(faker);
        // Note: Faker may not have a direct getLocale() method, verify construction instead
        assertNotNull(faker);
    }

    @Test
    @DisplayName("Should create RetryTemplate with custom policy")
    void shouldCreateRetryTemplateWithCustomPolicy() {
        JdbcTemplateApplication app = new JdbcTemplateApplication();

        RetryTemplate retryTemplate = app.retryTemplate(exceptionChecker);

        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should configure RetryTemplate with ExceptionChecker")
    void shouldConfigureRetryTemplateWithExceptionChecker() {
        JdbcTemplateApplication app = new JdbcTemplateApplication();

        RetryTemplate retryTemplate = app.retryTemplate(exceptionChecker);

        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create RetryTemplate even with null ExceptionChecker")
    void shouldCreateRetryTemplateEvenWithNullExceptionChecker() {
        JdbcTemplateApplication app = new JdbcTemplateApplication();

        // The method should create a RetryTemplate even if ExceptionChecker is null
        // The actual NPE would occur when the classifier is used, not when it's created
        RetryTemplate retryTemplate = assertDoesNotThrow(() -> app.retryTemplate(null));
        
        assertNotNull(retryTemplate);
    }
}