package com.jwebs.learn.fileprocess;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/customers")
public class CustomerController {
	private CustomerRepository customers;
	
	@RequestMapping(method=RequestMethod.GET)
	public HttpEntity<Iterable<Customer>> listCustomers() {
		return ResponseEntity.ok(customers.findAll());
	}
}
