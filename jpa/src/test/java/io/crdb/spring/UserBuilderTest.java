package io.crdb.spring;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBuilderTest {

    @Mock
    private Faker faker;

    private UserBuilder userBuilder;

    @BeforeEach
    void setUp() {
        userBuilder = new UserBuilder(faker);
        ReflectionTestUtils.setField(userBuilder, "rowSize", 5);
        
        // Setup default faker responses - need to mock the intermediate objects
        com.github.javafaker.Name mockName = mock(com.github.javafaker.Name.class);
        com.github.javafaker.Internet mockInternet = mock(com.github.javafaker.Internet.class);
        com.github.javafaker.Address mockAddress = mock(com.github.javafaker.Address.class);
        
        when(faker.name()).thenReturn(mockName);
        when(faker.internet()).thenReturn(mockInternet);
        when(faker.address()).thenReturn(mockAddress);
        
        when(mockName.firstName()).thenReturn("John");
        when(mockName.lastName()).thenReturn("Doe");
        when(mockInternet.safeEmailAddress()).thenReturn("john.doe@example.com");
        when(mockAddress.streetAddress()).thenReturn("123 Main St");
        when(mockAddress.city()).thenReturn("Anytown");
        when(mockAddress.stateAbbr()).thenReturn("NY");
        when(mockAddress.zipCode()).thenReturn("12345");
    }

    @Test
    @DisplayName("Should build single user with faker data")
    void shouldBuildSingleUserWithFakerData() {
        User result = userBuilder.buildUser();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Anytown", result.getCity());
        assertEquals("NY", result.getStateCode());
        assertEquals("12345", result.getZipCode());
        assertNotNull(result.getCreatedTimestamp());
        assertNull(result.getUpdatedTimestamp());
        
        verify(faker).name();
        verify(faker).internet();
        verify(faker).address();
    }

    @Test
    @DisplayName("Should build default number of users")
    void shouldBuildDefaultNumberOfUsers() {
        List<User> result = userBuilder.buildUsers();

        assertEquals(5, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
        }
        
        verify(faker, times(5)).name();
        verify(faker, times(5)).internet();
        verify(faker, times(5)).address();
    }

    @Test
    @DisplayName("Should build specified number of users")
    void shouldBuildSpecifiedNumberOfUsers() {
        List<User> result = userBuilder.buildUsers(10);

        assertEquals(10, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
        }
        
        verify(faker, times(10)).name();
        verify(faker, times(10)).internet();
        verify(faker, times(10)).address();
    }

    @Test
    @DisplayName("Should build zero users when requested")
    void shouldBuildZeroUsersWhenRequested() {
        List<User> result = userBuilder.buildUsers(0);

        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        
        verify(faker, never()).name();
        verify(faker, never()).internet();
        verify(faker, never()).address();
    }

    @Test
    @DisplayName("Should generate unique UUIDs for each user")
    void shouldGenerateUniqueUUIDsForEachUser() {
        List<User> result = userBuilder.buildUsers(100);
        
        Set<String> uniqueIds = new HashSet<>();
        for (User user : result) {
            uniqueIds.add(user.getId().toString());
        }
        
        assertEquals(100, uniqueIds.size());
    }

    @Test
    @DisplayName("Should handle null values from faker gracefully")
    void shouldHandleNullValuesFromFakerGracefully() {
        when(faker.name().firstName()).thenReturn(null);
        when(faker.name().lastName()).thenReturn(null);
        when(faker.internet().safeEmailAddress()).thenReturn(null);
        when(faker.address().streetAddress()).thenReturn(null);
        when(faker.address().city()).thenReturn(null);
        when(faker.address().stateAbbr()).thenReturn(null);
        when(faker.address().zipCode()).thenReturn(null);

        User result = userBuilder.buildUser();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNull(result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
        assertNull(result.getAddress());
        assertNull(result.getCity());
        assertNull(result.getStateCode());
        assertNull(result.getZipCode());
    }

    @Test
    @DisplayName("Should build large number of users efficiently")
    void shouldBuildLargeNumberOfUsersEfficiently() {
        List<User> result = userBuilder.buildUsers(1000);

        assertEquals(1000, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
        }
        
        verify(faker.name(), times(1000)).firstName();
        verify(faker.name(), times(1000)).lastName();
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple builds")
    void shouldMaintainDataIntegrityAcrossMultipleBuilds() {
        when(faker.name().firstName()).thenReturn("Alice", "Bob", "Charlie");
        when(faker.name().lastName()).thenReturn("Johnson", "Smith", "Wilson");
        
        List<User> firstBuild = userBuilder.buildUsers(3);
        List<User> secondBuild = userBuilder.buildUsers(3);
        
        assertEquals(3, firstBuild.size());
        assertEquals(3, secondBuild.size());
        
        // Verify that each build creates distinct objects
        assertNotEquals(firstBuild.get(0).getId(), secondBuild.get(0).getId());
        assertNotEquals(firstBuild.get(1).getId(), secondBuild.get(1).getId());
        assertNotEquals(firstBuild.get(2).getId(), secondBuild.get(2).getId());
    }

    @Test
    @DisplayName("Should handle negative number gracefully")
    void shouldHandleNegativeNumberGracefully() {
        List<User> result = userBuilder.buildUsers(-5);

        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        
        verify(faker, never()).name();
    }
}