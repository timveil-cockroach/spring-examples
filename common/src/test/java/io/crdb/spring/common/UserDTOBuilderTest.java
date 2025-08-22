package io.crdb.spring.common;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.github.javafaker.Address;
import com.github.javafaker.Internet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDTOBuilderTest {

    @Mock
    private Faker faker;
    
    @Mock
    private Name name;
    
    @Mock
    private Address address;
    
    @Mock
    private Internet internet;
    
    private UserDTOBuilder userDTOBuilder;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userDTOBuilder = new UserDTOBuilder(faker);
        
        when(faker.name()).thenReturn(name);
        when(faker.address()).thenReturn(address);
        when(faker.internet()).thenReturn(internet);
        
        ReflectionTestUtils.setField(userDTOBuilder, "rowSize", 128);
    }
    
    @Test
    @DisplayName("Should build a single user with faker data")
    void shouldBuildSingleUserWithFakerData() {
        String firstName = "John";
        String lastName = "Doe";
        String city = "New York";
        String state = "NY";
        String zipCode = "10001";
        
        when(name.firstName()).thenReturn(firstName);
        when(name.lastName()).thenReturn(lastName);
        when(internet.safeEmailAddress()).thenReturn("test@example.com");
        when(address.streetAddress()).thenReturn("123 Main St");
        when(address.city()).thenReturn(city);
        when(address.stateAbbr()).thenReturn(state);
        when(address.zipCode()).thenReturn(zipCode);
        
        UserDTO user = userDTOBuilder.buildUser();
        
        assertNotNull(user);
        assertNotNull(user.id());
        assertEquals(firstName, user.firstName());
        assertEquals(lastName, user.lastName());
        assertEquals(city, user.city());
        assertEquals(state, user.stateCode());
        assertEquals(zipCode, user.zipCode());
        
        verify(faker, times(2)).name();
        verify(faker).internet();
        verify(faker, times(4)).address();
        verify(name).firstName();
        verify(name).lastName();
        verify(internet).safeEmailAddress();
        verify(address).streetAddress();
        verify(address).city();
        verify(address).stateAbbr();
        verify(address).zipCode();
    }
    
    @Test
    @DisplayName("Should generate unique UUID for each user")
    void shouldGenerateUniqueUUIDForEachUser() {
        when(name.firstName()).thenReturn("Jane");
        when(name.lastName()).thenReturn("Smith");
        when(internet.safeEmailAddress()).thenReturn("jane@example.com");
        when(address.streetAddress()).thenReturn("456 Oak Ave");
        when(address.city()).thenReturn("Los Angeles");
        when(address.stateAbbr()).thenReturn("CA");
        when(address.zipCode()).thenReturn("90001");
        
        UserDTO user1 = userDTOBuilder.buildUser();
        UserDTO user2 = userDTOBuilder.buildUser();
        
        assertNotNull(user1.id());
        assertNotNull(user2.id());
        assertNotEquals(user1.id(), user2.id());
    }
    
    @Test
    @DisplayName("Should build specified number of users")
    void shouldBuildSpecifiedNumberOfUsers() {
        int numberOfUsers = 5;
        
        when(name.firstName()).thenReturn("Test");
        when(name.lastName()).thenReturn("User");
        when(internet.safeEmailAddress()).thenReturn("test@example.com");
        when(address.streetAddress()).thenReturn("789 Test Ave");
        when(address.city()).thenReturn("Test City");
        when(address.stateAbbr()).thenReturn("TS");
        when(address.zipCode()).thenReturn("12345");
        
        List<UserDTO> users = userDTOBuilder.buildUsers(numberOfUsers);
        
        assertNotNull(users);
        assertEquals(numberOfUsers, users.size());
        
        for (UserDTO user : users) {
            assertNotNull(user);
            assertNotNull(user.id());
            assertEquals("Test", user.firstName());
            assertEquals("User", user.lastName());
            assertEquals("Test City", user.city());
            assertEquals("TS", user.stateCode());
            assertEquals("12345", user.zipCode());
        }
        
        verify(faker, times(numberOfUsers * 2)).name();
        verify(faker, times(numberOfUsers)).internet();
        verify(faker, times(numberOfUsers * 4)).address();
    }
    
    @Test
    @DisplayName("Should build zero users when requested")
    void shouldBuildZeroUsersWhenRequested() {
        List<UserDTO> users = userDTOBuilder.buildUsers(0);
        
        assertNotNull(users);
        assertTrue(users.isEmpty());
        
        verify(faker, never()).name();
        verify(faker, never()).internet();
        verify(faker, never()).address();
    }
    
    @Test
    @DisplayName("Should use default row size when building users without parameter")
    void shouldUseDefaultRowSizeWhenBuildingUsersWithoutParameter() {
        int defaultRowSize = 128;
        ReflectionTestUtils.setField(userDTOBuilder, "rowSize", defaultRowSize);
        
        when(name.firstName()).thenReturn("Default");
        when(name.lastName()).thenReturn("Size");
        when(internet.safeEmailAddress()).thenReturn("default@example.com");
        when(address.streetAddress()).thenReturn("000 Default St");
        when(address.city()).thenReturn("Default City");
        when(address.stateAbbr()).thenReturn("DS");
        when(address.zipCode()).thenReturn("00000");
        
        List<UserDTO> users = userDTOBuilder.buildUsers();
        
        assertNotNull(users);
        assertEquals(defaultRowSize, users.size());
        
        verify(faker, times(defaultRowSize * 2)).name();
        verify(faker, times(defaultRowSize)).internet();
        verify(faker, times(defaultRowSize * 4)).address();
    }
    
    @Test
    @DisplayName("Should handle null values from faker gracefully")
    void shouldHandleNullValuesFromFakerGracefully() {
        when(name.firstName()).thenReturn(null);
        when(name.lastName()).thenReturn(null);
        when(internet.safeEmailAddress()).thenReturn(null);
        when(address.streetAddress()).thenReturn(null);
        when(address.city()).thenReturn(null);
        when(address.stateAbbr()).thenReturn(null);
        when(address.zipCode()).thenReturn(null);
        
        UserDTO user = userDTOBuilder.buildUser();
        
        assertNotNull(user);
        assertNotNull(user.id());
        assertNull(user.firstName());
        assertNull(user.lastName());
        assertNull(user.city());
        assertNull(user.stateCode());
        assertNull(user.zipCode());
    }
    
    @Test
    @DisplayName("Should build large number of users efficiently")
    void shouldBuildLargeNumberOfUsersEfficiently() {
        int largeNumber = 1000;
        
        when(name.firstName()).thenReturn("Large");
        when(name.lastName()).thenReturn("Test");
        when(internet.safeEmailAddress()).thenReturn("large@example.com");
        when(address.streetAddress()).thenReturn("999 Big Ave");
        when(address.city()).thenReturn("Big City");
        when(address.stateAbbr()).thenReturn("BC");
        when(address.zipCode()).thenReturn("99999");
        
        List<UserDTO> users = userDTOBuilder.buildUsers(largeNumber);
        
        assertNotNull(users);
        assertEquals(largeNumber, users.size());
        
        long distinctIds = users.stream()
            .map(UserDTO::id)
            .distinct()
            .count();
        
        assertEquals(largeNumber, distinctIds, "All user IDs should be unique");
    }
    
    @Test
    @DisplayName("Should maintain data integrity across multiple builds")
    void shouldMaintainDataIntegrityAcrossMultipleBuilds() {
        when(name.firstName()).thenReturn("Consistent");
        when(name.lastName()).thenReturn("Data");
        when(internet.safeEmailAddress()).thenReturn("consistent@example.com");
        when(address.streetAddress()).thenReturn("111 Same St");
        when(address.city()).thenReturn("Same City");
        when(address.stateAbbr()).thenReturn("SC");
        when(address.zipCode()).thenReturn("11111");
        
        List<UserDTO> firstBatch = userDTOBuilder.buildUsers(3);
        List<UserDTO> secondBatch = userDTOBuilder.buildUsers(3);
        
        assertEquals(3, firstBatch.size());
        assertEquals(3, secondBatch.size());
        
        firstBatch.forEach(user -> {
            assertEquals("Consistent", user.firstName());
            assertEquals("Data", user.lastName());
            assertEquals("Same City", user.city());
            assertEquals("SC", user.stateCode());
            assertEquals("11111", user.zipCode());
        });
        
        secondBatch.forEach(user -> {
            assertEquals("Consistent", user.firstName());
            assertEquals("Data", user.lastName());
            assertEquals("Same City", user.city());
            assertEquals("SC", user.stateCode());
            assertEquals("11111", user.zipCode());
        });
        
        boolean anyIdMatch = firstBatch.stream()
            .anyMatch(user1 -> secondBatch.stream()
                .anyMatch(user2 -> user1.id().equals(user2.id())));
        
        assertFalse(anyIdMatch, "IDs should be unique across batches");
    }
}