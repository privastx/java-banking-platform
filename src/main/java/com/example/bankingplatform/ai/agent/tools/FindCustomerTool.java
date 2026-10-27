package com.example.bankingplatform.ai.agent.tools;

import com.example.bankingplatform.ai.agent.models.CustomerIdentity;
import com.example.bankingplatform.customer.Customer;
import com.example.bankingplatform.customer.CustomerRepository;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FindCustomerTool {

    private static final Logger log = LoggerFactory.getLogger(FindCustomerTool.class);

    private final CustomerRepository customerRepository;

    public FindCustomerTool(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Tool("Find a customer by first and last name")
    public CustomerIdentity findCustomer(String firstName, String lastName) {
        log.info("Finding customer: {} {}", firstName, lastName);

        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty()) {
            return new CustomerIdentity(null, firstName, lastName);
        }

        try {
            Customer customer = customerRepository.findByFirstNameAndLastName(
                    firstName.trim(), lastName.trim()).orElse(null);

            if (customer != null) {
                return new CustomerIdentity(customer.getId(),
                        customer.getFirstName(), customer.getLastName());
            }
        } catch (Exception e) {
            log.error("Failed to find customer: {} {}", firstName, lastName, e);
        }

        return new CustomerIdentity(null, firstName, lastName);
    }
}