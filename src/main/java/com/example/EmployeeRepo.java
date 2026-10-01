package com.example;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepo extends CrudRepository<Employee, Integer>{


	List<Employee> findByState(String state);
}
