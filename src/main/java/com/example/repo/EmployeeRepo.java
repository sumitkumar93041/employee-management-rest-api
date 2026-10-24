package com.example.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.model.Employee;

public interface EmployeeRepo extends CrudRepository<Employee, Integer>{


	List<Employee> findByState(String state);
}
