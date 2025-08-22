package io.crdb.spring;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserBuilderTest {

    private UserBuilder userBuilder;

    @BeforeEach
    void setUp() {
        // Use a real Faker instance to avoid complex mock interactions
        Faker realFaker = new Faker();
        userBuilder = new UserBuilder(realFaker);
        ReflectionTestUtils.setField(userBuilder, "rowSize", 5);
    }

    @Test
    @DisplayName("Should build single user with faker data")
    void shouldBuildSingleUserWithFakerData() {
        User result = userBuilder.buildUser();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getFirstName());
        assertNotNull(result.getLastName());
        assertNotNull(result.getEmail());
        assertNotNull(result.getAddress());
        assertNotNull(result.getCity());
        assertNotNull(result.getStateCode());
        assertNotNull(result.getZipCode());
        assertNotNull(result.getCreatedTimestamp());
        assertNull(result.getUpdatedTimestamp());
    }

    @Test
    @DisplayName("Should build default number of users")
    void shouldBuildDefaultNumberOfUsers() {
        List<User> result = userBuilder.buildUsers();

        assertEquals(5, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
            assertNotNull(user.getFirstName());
            assertNotNull(user.getLastName());
        }
    }

    @Test
    @DisplayName("Should build specified number of users")
    void shouldBuildSpecifiedNumberOfUsers() {
        List<User> result = userBuilder.buildUsers(10);

        assertEquals(10, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
            assertNotNull(user.getFirstName());
            assertNotNull(user.getLastName());
        }
    }

    @Test
    @DisplayName("Should build zero users when requested")
    void shouldBuildZeroUsersWhenRequested() {
        List<User> result = userBuilder.buildUsers(0);

        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
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
    @DisplayName("Should build large number of users efficiently")
    void shouldBuildLargeNumberOfUsersEfficiently() {
        List<User> result = userBuilder.buildUsers(1000);

        assertEquals(1000, result.size());
        for (User user : result) {
            assertNotNull(user);
            assertNotNull(user.getId());
        }
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple builds")
    void shouldMaintainDataIntegrityAcrossMultipleBuilds() {
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
    }

    @Test
    @DisplayName("Should handle edge case inputs")
    void shouldHandleEdgeCaseInputs() {
        // Test with edge case values
        List<User> result1 = userBuilder.buildUsers(1);
        assertEquals(1, result1.size());

        // Test single user creation
        User singleUser = userBuilder.buildUser();
        assertNotNull(singleUser);
        assertNotNull(singleUser.getId());
    }
}