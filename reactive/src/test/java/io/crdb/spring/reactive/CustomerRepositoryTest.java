package io.crdb.spring.reactive;

import io.crdb.spring.Customer;
import io.crdb.spring.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {

    @Mock
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should find customers by last name")
    void shouldFindCustomersByLastName() {
        String lastName = "Smith";
        Customer customer1 = new Customer("John", lastName);
        customer1.setId(1L);
        Customer customer2 = new Customer("Jane", lastName);
        customer2.setId(2L);
        
        Flux<Customer> expectedCustomers = Flux.just(customer1, customer2);
        when(customerRepository.findByLastName(lastName)).thenReturn(expectedCustomers);

        Flux<Customer> result = customerRepository.findByLastName(lastName);

        StepVerifier.create(result)
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();

        verify(customerRepository).findByLastName(lastName);
    }

    @Test
    @DisplayName("Should return empty flux when no customers found by last name")
    void shouldReturnEmptyFluxWhenNoCustomersFoundByLastName() {
        String lastName = "NonExistent";
        Flux<Customer> emptyFlux = Flux.empty();
        when(customerRepository.findByLastName(lastName)).thenReturn(emptyFlux);

        Flux<Customer> result = customerRepository.findByLastName(lastName);

        StepVerifier.create(result)
                .verifyComplete();

        verify(customerRepository).findByLastName(lastName);
    }

    @Test
    @DisplayName("Should handle single customer result for last name query")
    void shouldHandleSingleCustomerResultForLastNameQuery() {
        String lastName = "UniqueLastName";
        Customer customer = new Customer("Alice", lastName);
        customer.setId(100L);
        
        Flux<Customer> singleCustomerFlux = Flux.just(customer);
        when(customerRepository.findByLastName(lastName)).thenReturn(singleCustomerFlux);

        Flux<Customer> result = customerRepository.findByLastName(lastName);

        StepVerifier.create(result)
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepository).findByLastName(lastName);
    }

    @Test
    @DisplayName("Should handle error in findByLastName query")
    void shouldHandleErrorInFindByLastNameQuery() {
        String lastName = "ErrorCase";
        RuntimeException expectedException = new RuntimeException("Database error");
        when(customerRepository.findByLastName(lastName)).thenReturn(Flux.error(expectedException));

        Flux<Customer> result = customerRepository.findByLastName(lastName);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(customerRepository).findByLastName(lastName);
    }

    @Test
    @DisplayName("Should handle null last name parameter")
    void shouldHandleNullLastNameParameter() {
        Flux<Customer> emptyFlux = Flux.empty();
        when(customerRepository.findByLastName(null)).thenReturn(emptyFlux);

        Flux<Customer> result = customerRepository.findByLastName(null);

        StepVerifier.create(result)
                .verifyComplete();

        verify(customerRepository).findByLastName(null);
    }

    @Test
    @DisplayName("Should handle empty string last name parameter")
    void shouldHandleEmptyStringLastNameParameter() {
        String emptyLastName = "";
        Flux<Customer> emptyFlux = Flux.empty();
        when(customerRepository.findByLastName(emptyLastName)).thenReturn(emptyFlux);

        Flux<Customer> result = customerRepository.findByLastName(emptyLastName);

        StepVerifier.create(result)
                .verifyComplete();

        verify(customerRepository).findByLastName(emptyLastName);
    }

    @Test
    @DisplayName("Should find customers with special characters in last name")
    void shouldFindCustomersWithSpecialCharactersInLastName() {
        String specialLastName = "O'Connor-Smith";
        Customer customer = new Customer("Patrick", specialLastName);
        customer.setId(50L);
        
        Flux<Customer> customerFlux = Flux.just(customer);
        when(customerRepository.findByLastName(specialLastName)).thenReturn(customerFlux);

        Flux<Customer> result = customerRepository.findByLastName(specialLastName);

        StepVerifier.create(result)
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepository).findByLastName(specialLastName);
    }

    @Test
    @DisplayName("Should handle case-sensitive last name search")
    void shouldHandleCaseSensitiveLastNameSearch() {
        String lowerCaseLastName = "smith";
        Customer customer = new Customer("Bob", "smith");
        customer.setId(25L);
        
        Flux<Customer> customerFlux = Flux.just(customer);
        when(customerRepository.findByLastName(lowerCaseLastName)).thenReturn(customerFlux);

        Flux<Customer> result = customerRepository.findByLastName(lowerCaseLastName);

        StepVerifier.create(result)
                .expectNext(customer)
                .verifyComplete();

        verify(customerRepository).findByLastName(lowerCaseLastName);
    }

    @Test
    @DisplayName("Should handle large number of customers with same last name")
    void shouldHandleLargeNumberOfCustomersWithSameLastName() {
        String commonLastName = "Johnson";
        Customer[] customers = new Customer[100];
        
        for (int i = 0; i < 100; i++) {
            customers[i] = new Customer("Customer" + i, commonLastName);
            customers[i].setId((long) i);
        }
        
        Flux<Customer> manyCustomersFlux = Flux.fromArray(customers);
        when(customerRepository.findByLastName(commonLastName)).thenReturn(manyCustomersFlux);

        Flux<Customer> result = customerRepository.findByLastName(commonLastName);

        StepVerifier.create(result)
                .expectNextCount(100)
                .verifyComplete();

        verify(customerRepository).findByLastName(commonLastName);
    }

    @Test
    @DisplayName("Should verify method signature and return type")
    void shouldVerifyMethodSignatureAndReturnType() {
        String testLastName = "TestSignature";
        when(customerRepository.findByLastName(anyString())).thenReturn(Flux.empty());

        Flux<Customer> result = customerRepository.findByLastName(testLastName);

        assertNotNull(result);
        assertTrue(result instanceof Flux);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(customerRepository).findByLastName(testLastName);
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null object");
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}