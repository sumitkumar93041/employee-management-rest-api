package com.example.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.model.Employee;

@DataJpaTest
public class EmployeeRepoTest {

	@Autowired
	private EmployeeRepo employeeRepo;

	@Test
	void shouldSaveAndFindEmployeeById() {
		Employee employee = new Employee(175, "Vaibhav", "Gupta", 23, 50000.0, "Lucknow", "Uttar Pradesh");

		employeeRepo.save(employee);
		Optional<Employee> found = employeeRepo.findById(175);

		assertTrue(found.isPresent());
		assertEquals("Vaibhav", found.get().getFirst_name());
		assertEquals("Lucknow", found.get().getCity());
	}

	@Test
	void shouldReturnEmptyWhenIdDoesNotExist() {
		assertTrue(employeeRepo.findById(999).isEmpty());
	}

	@Test
	void shouldCheckIfIdExists() {
		employeeRepo.save(new Employee(175, "Vaibhav", "Gupta", 23, 50000.0, "Lucknow", "Uttar Pradesh"));

		assertTrue(employeeRepo.existsById(175));
		assertFalse(employeeRepo.existsById(999));
	}

	@Test
	void shouldFindOnlyEmployeesOfGivenState() {
		employeeRepo.save(new Employee(1, "Amit", "Kumar", 25, 45000.0, "Patna", "Bihar"));
		employeeRepo.save(new Employee(2, "Ravi", "Singh", 28, 52000.0, "Gaya", "Bihar"));
		employeeRepo.save(new Employee(3, "Vaibhav", "Gupta", 23, 50000.0, "Lucknow", "Uttar Pradesh"));

		List<Employee> result = employeeRepo.findByState("Bihar");

		assertEquals(2, result.size());
		assertTrue(result.stream().allMatch(e -> e.getState().equals("Bihar")));
	}

	@Test
	void shouldReturnEmptyListWhenNoEmployeeInState() {
		employeeRepo.save(new Employee(1, "Amit", "Kumar", 25, 45000.0, "Patna", "Bihar"));

		List<Employee> result = employeeRepo.findByState("Goa");

		assertTrue(result.isEmpty());
	}

	@Test
	void shouldDeleteEmployeeById() {
		employeeRepo.save(new Employee(1, "Amit", "Kumar", 25, 45000.0, "Patna", "Bihar"));

		employeeRepo.deleteById(1);

		assertFalse(employeeRepo.existsById(1));
		assertEquals(0, employeeRepo.count());
	}
}