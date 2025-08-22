package io.crdb.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private User testUser;
    private List<User> testUsers;
    private List<UUID> testIds;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
        testUser = createTestUser();
        testUsers = Arrays.asList(testUser, createTestUser(), createTestUser());
        testIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        when(userRepository.findAll()).thenReturn(testUsers);

        Iterable<User> result = userService.findAll();

        assertEquals(testUsers, result);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should find all users by IDs")
    void shouldFindAllUsersByIds() {
        when(userRepository.findAllById(testIds)).thenReturn(testUsers);

        Iterable<User> result = userService.findAll(testIds);

        assertEquals(testUsers, result);
        verify(userRepository).findAllById(testIds);
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.find(testUser.getId());

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should return empty optional when user not found")
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<User> result = userService.find(nonExistentId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should check if user exists")
    void shouldCheckIfUserExists() {
        when(userRepository.existsById(testUser.getId())).thenReturn(true);

        boolean result = userService.exists(testUser.getId());

        assertTrue(result);
        verify(userRepository).existsById(testUser.getId());
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void shouldReturnFalseWhenUserDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        boolean result = userService.exists(nonExistentId);

        assertFalse(result);
        verify(userRepository).existsById(nonExistentId);
    }

    @Test
    @DisplayName("Should count all users")
    void shouldCountAllUsers() {
        when(userRepository.count()).thenReturn(5L);

        long result = userService.count();

        assertEquals(5L, result);
        verify(userRepository).count();
    }

    @Test
    @DisplayName("Should save all users from list")
    void shouldSaveAllUsersFromList() {
        when(userRepository.saveAll(testUsers)).thenReturn(testUsers);

        Iterable<User> result = userService.saveAll(testUsers);

        assertEquals(testUsers, result);
        verify(userRepository).saveAll(testUsers);
    }

    @Test
    @DisplayName("Should save all users from iterable")
    void shouldSaveAllUsersFromIterable() {
        Iterable<User> userIterable = testUsers;
        when(userRepository.saveAll(userIterable)).thenReturn(testUsers);

        Iterable<User> result = userService.saveAll(userIterable);

        assertEquals(testUsers, result);
        verify(userRepository).saveAll(userIterable);
    }

    @Test
    @DisplayName("Should save single user")
    void shouldSaveSingleUser() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.save(testUser);

        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should delete all users")
    void shouldDeleteAllUsers() {
        doNothing().when(userRepository).deleteAll();

        assertDoesNotThrow(() -> userService.deleteAll());

        verify(userRepository).deleteAll();
    }

    @Test
    @DisplayName("Should delete all specified users")
    void shouldDeleteAllSpecifiedUsers() {
        doNothing().when(userRepository).deleteAll(testUsers);

        assertDoesNotThrow(() -> userService.deleteAll(testUsers));

        verify(userRepository).deleteAll(testUsers);
    }

    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUserById() {
        doNothing().when(userRepository).deleteById(testUser.getId());

        assertDoesNotThrow(() -> userService.delete(testUser.getId()));

        verify(userRepository).deleteById(testUser.getId());
    }

    @Test
    @DisplayName("Should delete user entity")
    void shouldDeleteUserEntity() {
        doNothing().when(userRepository).delete(testUser);

        assertDoesNotThrow(() -> userService.delete(testUser));

        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Should handle empty user list for save all")
    void shouldHandleEmptyUserListForSaveAll() {
        List<User> emptyList = Arrays.asList();
        when(userRepository.saveAll(emptyList)).thenReturn(emptyList);

        Iterable<User> result = userService.saveAll(emptyList);

        assertEquals(emptyList, result);
        verify(userRepository).saveAll(emptyList);
    }

    @Test
    @DisplayName("Should handle null user gracefully")
    void shouldHandleNullUserGracefully() {
        when(userRepository.save(null)).thenReturn(null);

        User result = userService.save(null);

        assertNull(result);
        verify(userRepository).save(null);
    }

    private User createTestUser() {
        return new User(
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