package io.crdb.spring.reactive;

import io.crdb.spring.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    @DisplayName("Should create customer with parameterized constructor")
    void shouldCreateCustomerWithParameterizedConstructor() {
        String firstName = "John";
        String lastName = "Doe";
        
        Customer customer = new Customer(firstName, lastName);
        
        assertNotNull(customer);
        assertNull(customer.getId()); // ID should be null initially
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
    }

    @Test
    @DisplayName("Should set and get ID correctly")
    void shouldSetAndGetIdCorrectly() {
        Customer customer = new Customer("Alice", "Smith");
        Long expectedId = 12345L;
        
        customer.setId(expectedId);
        
        assertEquals(expectedId, customer.getId());
    }

    @Test
    @DisplayName("Should have immutable first and last names")
    void shouldHaveImmutableFirstAndLastNames() {
        String firstName = "Bob";
        String lastName = "Johnson";
        
        Customer customer = new Customer(firstName, lastName);
        
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        
        // Names cannot be changed after construction (no setters)
        assertNotNull(customer.getFirstName());
        assertNotNull(customer.getLastName());
    }

    @Test
    @DisplayName("Should handle null values in constructor")
    void shouldHandleNullValuesInConstructor() {
        Customer customer = new Customer(null, null);
        
        assertNotNull(customer);
        assertNull(customer.getId());
        assertNull(customer.getFirstName());
        assertNull(customer.getLastName());
    }

    @Test
    @DisplayName("Should handle empty strings for name fields")
    void shouldHandleEmptyStringsForNameFields() {
        Customer customer = new Customer("", "");
        
        assertNotNull(customer);
        assertEquals("", customer.getFirstName());
        assertEquals("", customer.getLastName());
    }

    @Test
    @DisplayName("Should handle special characters in names")
    void shouldHandleSpecialCharactersInNames() {
        String specialFirstName = "José-María";
        String specialLastName = "O'Connor-Smith";
        
        Customer customer = new Customer(specialFirstName, specialLastName);
        
        assertEquals(specialFirstName, customer.getFirstName());
        assertEquals(specialLastName, customer.getLastName());
    }

    @Test
    @DisplayName("Should handle very long names")
    void shouldHandleVeryLongNames() {
        String longFirstName = "A".repeat(1000);
        String longLastName = "B".repeat(1000);
        
        Customer customer = new Customer(longFirstName, longLastName);
        
        assertEquals(longFirstName, customer.getFirstName());
        assertEquals(longLastName, customer.getLastName());
        assertEquals(1000, customer.getFirstName().length());
        assertEquals(1000, customer.getLastName().length());
    }

    @Test
    @DisplayName("Should support ID modification after construction")
    void shouldSupportIdModificationAfterConstruction() {
        Customer customer = new Customer("Initial", "Name");
        
        assertEquals("Initial", customer.getFirstName());
        assertEquals("Name", customer.getLastName());
        assertNull(customer.getId());
        
        customer.setId(100L);
        assertEquals(100L, customer.getId());
        
        // Names should remain unchanged
        assertEquals("Initial", customer.getFirstName());
        assertEquals("Name", customer.getLastName());
        
        // Can update ID again
        customer.setId(200L);
        assertEquals(200L, customer.getId());
    }

    @Test
    @DisplayName("Should handle toString method correctly")
    void shouldHandleToStringMethodCorrectly() {
        Customer customer = new Customer("John", "Doe");
        customer.setId(123L);
        
        String toString = customer.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Customer"));
        assertTrue(toString.contains("123"));
        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("Doe"));
        assertEquals("Customer[id=123, firstName='John', lastName='Doe']", toString);
    }

    @Test
    @DisplayName("Should handle toString with null ID")
    void shouldHandleToStringWithNullId() {
        Customer customer = new Customer("Jane", "Smith");
        
        String toString = customer.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("Customer"));
        assertTrue(toString.contains("null"));
        assertTrue(toString.contains("Jane"));
        assertTrue(toString.contains("Smith"));
    }

    @Test
    @DisplayName("Should ensure immutability of name fields")
    void shouldEnsureImmutabilityOfNameFields() {
        String firstName = "Test";
        String lastName = "User";
        
        Customer customer = new Customer(firstName, lastName);
        
        // Verify names are set correctly
        assertEquals(firstName, customer.getFirstName());
        assertEquals(lastName, customer.getLastName());
        
        // Modify the original strings (should not affect the customer)
        firstName = "Modified";
        lastName = "Changed";
        
        // Customer names should remain unchanged
        assertEquals("Test", customer.getFirstName());
        assertEquals("User", customer.getLastName());
    }
}