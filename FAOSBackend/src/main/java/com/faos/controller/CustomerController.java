package com.faos.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.faos.exception.InvalidEntityException;
import com.faos.model.Customer;
import com.faos.model.Login;
import com.faos.service.CustomerService;

import jakarta.validation.Valid;

@RestController
public class CustomerController {
	@Autowired
	private CustomerService customerService;

	@PostMapping("/register")
	public ResponseEntity<?> registerCustomer(@Validated @RequestBody Customer customer, BindingResult result) {
		if (result.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			result.getFieldErrors().forEach(error ->
					errors.put(error.getField(), error.getDefaultMessage())
			);
			return ResponseEntity.badRequest().body(errors);
		}

		try {
            customerService.validateCustomerData(customer);
        } catch (InvalidEntityException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
		
		if (customerService.isEmailExists(customer.getEmail())) {
	        return ResponseEntity.badRequest().body("Customer with this email already exists.");
	    }
	    if (customerService.isContactExists(customer.getContactNo())) {
	    	 return ResponseEntity.badRequest().body("Customer with this Contact already exists.");
	    }
 
		Customer registeredCustomer = customerService.registerCustomer(customer);
		return ResponseEntity.ok(registeredCustomer);
	}
	
	 //Get details of all customers
	 @GetMapping("/getAllCustomers")
	    public ResponseEntity<?>  getAllCustomers() {
		  
	         List<Customer> activeCustomers= customerService.getAllCustomers();
	         if (activeCustomers.isEmpty()) {
	             // Return a response with a message and HTTP 404 (Not Found) status
	             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active customers found.");
	         }
	         
	         // Return the list of active customers with HTTP 200 (OK) status
	         return ResponseEntity.ok(activeCustomers);
	    }
	
	// For Fetching the customer details for updation
	@GetMapping("/customer/{id}")
		public ResponseEntity<?> getCustomerById(@PathVariable("id") String consumerId) {
			try {
				Customer customer = customerService.getCustomerById(consumerId);
				return ResponseEntity.ok(customer);
			} catch (InvalidEntityException e) {
				throw new InvalidEntityException("Customer not found with ID: " + consumerId);
			}
		}
	 
	
	
	//Update the Customer Details
	@PutMapping("/customer/{id}")
		public ResponseEntity<?> updateCustomer(
				@PathVariable("id") String consumerId,
				@Valid @RequestBody Customer updatedCustomer,
				BindingResult result) {
			if (result.hasErrors()) {
				StringBuilder errors = new StringBuilder();
				result.getAllErrors().forEach(error ->
						errors.append(error.getDefaultMessage()).append(". "));
				throw new InvalidEntityException(errors.toString());
			}
			try {
	            customerService.validateCustomerData(updatedCustomer);
	        } catch (InvalidEntityException ex) {
	            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
	        }
			try {
				Customer updated = customerService.updateCustomer(consumerId, updatedCustomer);
				return ResponseEntity.ok(updated);
			} catch (InvalidEntityException e) {
				throw new InvalidEntityException("Error updating customer: " + e.getMessage());
			}
		}
	 
	 //Deactivate the customer
	 @PutMapping("/customers/deactivate/{id}")
	 public ResponseEntity<?> deactivateCustomer(@PathVariable("id") String consumerId) {
	     try {
	         customerService.deactivateCustomer(consumerId);
	         return ResponseEntity.ok("Customer with ID: " + consumerId + " has been deactivated.");
	     } catch (InvalidEntityException e) {
	         return ResponseEntity.badRequest().body(e.getMessage());
	     }
	 }

	 //Report Management
	 
}