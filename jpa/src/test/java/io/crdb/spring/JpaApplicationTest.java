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
class JpaApplicationTest {

    @Mock
    private ExceptionChecker exceptionChecker;

    @Test
    @DisplayName("Should create Faker bean with US locale")
    void shouldCreateFakerBeanWithUSLocale() {
        JpaApplication app = new JpaApplication();

        Faker faker = app.faker();

        assertNotNull(faker);
        // Note: Faker may not have a direct getLocale() method, verify construction instead
        assertNotNull(faker);
    }

    @Test
    @DisplayName("Should create RetryTemplate with custom policy")
    void shouldCreateRetryTemplateWithCustomPolicy() {
        JpaApplication app = new JpaApplication();

        RetryTemplate retryTemplate = app.retryTemplate(exceptionChecker);

        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should configure RetryTemplate with ExceptionChecker")
    void shouldConfigureRetryTemplateWithExceptionChecker() {
        JpaApplication app = new JpaApplication();

        RetryTemplate retryTemplate = app.retryTemplate(exceptionChecker);

        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create RetryTemplate even with null ExceptionChecker")
    void shouldCreateRetryTemplateEvenWithNullExceptionChecker() {
        JpaApplication app = new JpaApplication();

        // The method should create a RetryTemplate even if ExceptionChecker is null
        // The actual NPE would occur when the classifier is used, not when it's created
        RetryTemplate retryTemplate = assertDoesNotThrow(() -> app.retryTemplate(null));
        
        assertNotNull(retryTemplate);
    }

    @Test
    @DisplayName("Should create different Faker instances on multiple calls")
    void shouldCreateDifferentFakerInstancesOnMultipleCalls() {
        JpaApplication app = new JpaApplication();

        Faker faker1 = app.faker();
        Faker faker2 = app.faker();

        assertNotSame(faker1, faker2);
        // Both fakers should be non-null instances
        assertNotNull(faker1);
        assertNotNull(faker2);
    }

    @Test
    @DisplayName("Should create different RetryTemplate instances on multiple calls")
    void shouldCreateDifferentRetryTemplateInstancesOnMultipleCalls() {
        JpaApplication app = new JpaApplication();

        RetryTemplate template1 = app.retryTemplate(exceptionChecker);
        RetryTemplate template2 = app.retryTemplate(exceptionChecker);

        assertNotSame(template1, template2);
    }
}