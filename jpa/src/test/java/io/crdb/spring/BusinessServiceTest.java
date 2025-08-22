package io.crdb.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private UserRepository userRepository;

    private BusinessService businessService;
    private User testUser;

    @BeforeEach
    void setUp() {
        businessService = new BusinessService(userRepository);
        testUser = createTestUser();
    }

    @Test
    @DisplayName("Should execute complex service without force retry")
    void shouldExecuteComplexServiceWithoutForceRetry() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenReturn(1L);

        assertDoesNotThrow(() -> businessService.someComplexService(testUser, false));

        verify(userRepository, times(2)).save(testUser);
        verify(userRepository).count();
        verify(userRepository, never()).forceRetry();
    }

    @Test
    @DisplayName("Should execute complex service with force retry")
    void shouldExecuteComplexServiceWithForceRetry() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenReturn(1L);
        doNothing().when(userRepository).forceRetry();

        assertDoesNotThrow(() -> businessService.someComplexService(testUser, true));

        verify(userRepository, times(2)).save(testUser);
        verify(userRepository).count();
        verify(userRepository).forceRetry();
    }

    @Test
    @DisplayName("Should handle repository exceptions during save")
    void shouldHandleRepositoryExceptionsDuringSave() {
        when(userRepository.save(testUser)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> businessService.someComplexService(testUser, false));

        verify(userRepository).save(testUser);
        verify(userRepository, never()).count();
        verify(userRepository, never()).forceRetry();
    }

    @Test
    @DisplayName("Should handle repository exceptions during count")
    void shouldHandleRepositoryExceptionsDuringCount() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenThrow(new RuntimeException("Count error"));

        assertThrows(RuntimeException.class, () -> businessService.someComplexService(testUser, false));

        verify(userRepository).save(testUser);
        verify(userRepository).count();
        verify(userRepository, never()).forceRetry();
    }

    @Test
    @DisplayName("Should handle force retry exception")
    void shouldHandleForceRetryException() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenReturn(1L);
        doThrow(new RuntimeException("Force retry error")).when(userRepository).forceRetry();

        assertThrows(RuntimeException.class, () -> businessService.someComplexService(testUser, true));

        verify(userRepository, times(2)).save(testUser);
        verify(userRepository).count();
        verify(userRepository).forceRetry();
    }

    @Test
    @DisplayName("Should execute all steps when no exceptions occur")
    void shouldExecuteAllStepsWhenNoExceptionsOccur() {
        User savedUser = createTestUser();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.count()).thenReturn(5L);

        assertDoesNotThrow(() -> businessService.someComplexService(testUser, false));

        verify(userRepository, times(2)).save(any(User.class));
        verify(userRepository).count();
    }

    @Test
    @DisplayName("Should handle null user input")
    void shouldHandleNullUserInput() {
        // The method will throw NPE when trying to access user.getStateCode() on null user
        assertThrows(NullPointerException.class, () -> businessService.someComplexService(null, false));

        verify(userRepository).save(null);
        // count() and the second save() won't be called due to the early NPE
        verify(userRepository, never()).count();
    }

    @Test
    @DisplayName("Should call force retry only when flag is true")
    void shouldCallForceRetryOnlyWhenFlagIsTrue() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenReturn(1L);

        // Test with forceRetry = false
        businessService.someComplexService(testUser, false);
        verify(userRepository, never()).forceRetry();

        reset(userRepository);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userRepository.count()).thenReturn(1L);

        // Test with forceRetry = true
        businessService.someComplexService(testUser, true);
        verify(userRepository).forceRetry();
    }

    private User createTestUser() {
        return new User(
            UUID.randomUUID(),
            "Jane",
            "Smith", 
            "jane.smith@example.com",
            "456 Oak Ave",
            "Springfield",
            "CA",
            "90210",
            ZonedDateTime.now(),
            null
        );
    }
}