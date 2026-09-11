package com.example.bankingplatform.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_shouldSaveAndReturnCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@email.com");
        customer.setCountry("US");

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void getById_shouldReturnCustomerWhenExists() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Jane");
        customer.setEmail("jane@email.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void getById_shouldThrowExceptionWhenNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.getById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void updateCustomer_shouldUpdateAndReturnCustomer() {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setFirstName("Old");
        existingCustomer.setEmail("old@email.com");
        existingCustomer.setCountry("US");
        existingCustomer.setStatus(CustomerStatus.ACTIVE);

        Customer updateRequest = new Customer();
        updateRequest.setFirstName("New");
        updateRequest.setEmail("new@email.com");
        updateRequest.setCountry("GB");
        updateRequest.setStatus(CustomerStatus.SUSPENDED);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(existingCustomer);

        Customer result = customerService.updateCustomer(1L, updateRequest);

        assertEquals("New", result.getFirstName());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("GB", result.getCountry());
        assertEquals(CustomerStatus.SUSPENDED, result.getStatus());
    }

    @Test
    void findAll_shouldReturnAllCustomers() {
        Customer customer1 = new Customer();
        customer1.setId(1L);
        customer1.setFirstName("John");

        Customer customer2 = new Customer();
        customer2.setId(2L);
        customer2.setFirstName("Jane");

        when(customerRepository.findAll()).thenReturn(java.util.List.of(customer1, customer2));

        var result = customerService.findAll();

        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void deleteCustomer_shouldDeleteCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository, times(1)).delete(customer);
    }
}