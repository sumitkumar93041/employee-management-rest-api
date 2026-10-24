package com.example.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Employee;
import com.example.repo.EmployeeRepo;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

	@Mock
	private EmployeeRepo employeeRepo;

	@InjectMocks
	private EmployeeService employeeService;

	private Employee vaibhav() {
		return new Employee(175, "Vaibhav", "Gupta", 23, 50000.0, "Lucknow", "Uttar Pradesh");
	}

	private Employee amit() {
		return new Employee(176, "Amit", "Kumar", 25, 45000.0, "Patna", "Bihar");
	}

	@Test
	void shouldReturnAllEmployees() {
		when(employeeRepo.findAll()).thenReturn(List.of(vaibhav(), amit()));

		List<Employee> result = employeeService.getAllEmp();

		assertEquals(2, result.size());
		assertEquals("Vaibhav", result.get(0).getFirst_name());
		assertEquals("Amit", result.get(1).getFirst_name());

		verify(employeeRepo).findAll();
	}

	@Test
	void shouldReturnEmptyListWhenNoEmployeeExists() {
		when(employeeRepo.findAll()).thenReturn(List.of());

		List<Employee> result = employeeService.getAllEmp();

		assertTrue(result.isEmpty());
		verify(employeeRepo).findAll();
	}

	@Test
	void shouldReturnEmployeesOfGivenState() {
		when(employeeRepo.findByState("Bihar")).thenReturn(List.of(amit()));

		List<Employee> result = employeeService.getEmpByState("Bihar");

		assertEquals(1, result.size());
		assertEquals("Bihar", result.get(0).getState());

		verify(employeeRepo).findByState("Bihar");
	}

	@Test
	void shouldReturnEmptyListWhenNoEmployeeInState() {
		when(employeeRepo.findByState("Goa")).thenReturn(List.of());

		List<Employee> result = employeeService.getEmpByState("Goa");

		assertTrue(result.isEmpty());
		verify(employeeRepo).findByState("Goa");
	}

	@Test
	void shouldReturnEmployeeWhenIdExists() {
		when(employeeRepo.findById(175)).thenReturn(Optional.of(vaibhav()));

		Employee result = employeeService.getEmplByid(175);

		assertEquals(175, result.getId());
		assertEquals("Vaibhav", result.getFirst_name());

		verify(employeeRepo).findById(175);
	}

	@Test
	void shouldThrowNotFoundWhenEmployeeIdDoesNotExist() {
		when(employeeRepo.findById(99)).thenReturn(Optional.empty());

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> employeeService.getEmplByid(99));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Employee Not Found", exception.getReason());

		verify(employeeRepo).findById(99);
	}

	@Test
	void shouldSaveEmployeeWhenIdIsNew() {
		Employee request = vaibhav();
		when(employeeRepo.existsById(175)).thenReturn(false);
		when(employeeRepo.save(request)).thenReturn(request);

		Employee result = employeeService.addEmployee(request);

		assertEquals(175, result.getId());
		assertEquals("Vaibhav", result.getFirst_name());

		verify(employeeRepo).existsById(175);
		verify(employeeRepo).save(request);
	}

	@Test
	void shouldRejectEmployeeWhenIdAlreadyExists() {
		Employee request = vaibhav();
		when(employeeRepo.existsById(175)).thenReturn(true);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> employeeService.addEmployee(request));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		assertEquals("Employee with this id already exist", exception.getReason());

		verify(employeeRepo).existsById(175);
		verify(employeeRepo, never()).save(any(Employee.class));
	}

	@Test
	void shouldDeleteEmployeeWhenIdExists() {
		when(employeeRepo.existsById(175)).thenReturn(true);

		employeeService.DeleteEmployee(175);

		verify(employeeRepo).existsById(175);
		verify(employeeRepo).deleteById(175);
	}

	@Test
	void shouldThrowNotFoundWhenDeletingMissingEmployee() {
		when(employeeRepo.existsById(99)).thenReturn(false);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> employeeService.DeleteEmployee(99));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("Employee Not Found", exception.getReason());

		verify(employeeRepo, never()).deleteById(anyInt());
	}

	@Test
	void shouldUpdateEmployeeAndUseIdFromPath() {
		Employee request = new Employee(5, "Vaibhav", "Gupta", 24, 60000.0, "Delhi", "Delhi");
		when(employeeRepo.existsById(175)).thenReturn(true);
		when(employeeRepo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Employee result = employeeService.EmployeeUpdate(request, 175);

		assertEquals(175, result.getId());
		assertEquals("Delhi", result.getCity());

		ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
		verify(employeeRepo).save(captor.capture());
		assertEquals(175, captor.getValue().getId());
		assertEquals(60000.0, captor.getValue().getSalary());
	}

	@Test
	void shouldThrowNotFoundWhenUpdatingMissingEmployee() {
		Employee request = vaibhav();
		when(employeeRepo.existsById(99)).thenReturn(false);

		ResponseStatusException exception =
				assertThrows(ResponseStatusException.class, () -> employeeService.EmployeeUpdate(request, 99));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verify(employeeRepo, never()).save(any(Employee.class));
	}
}