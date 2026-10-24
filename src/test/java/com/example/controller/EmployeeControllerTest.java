package com.example.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Employee;
import com.example.service.EmployeeService;
import com.example.service.JWTService;

@WebMvcTest(EmployeeController.class)
@WithMockUser
public class EmployeeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EmployeeService employeeService;

	@MockitoBean
	private JWTService jwtService;

	private static final String EMPLOYEE_JSON =
			"{\"id\":175,\"first_name\":\"Vaibhav\",\"last_name\":\"Gupta\",\"age\":23,"
			+ "\"salary\":50000.0,\"city\":\"Lucknow\",\"state\":\"Uttar Pradesh\"}";

	private Employee vaibhav() {
		return new Employee(175, "Vaibhav", "Gupta", 23, 50000.0, "Lucknow", "Uttar Pradesh");
	}

	private Employee amit() {
		return new Employee(176, "Amit", "Kumar", 25, 45000.0, "Patna", "Bihar");
	}


	@Test
	void shouldReturnAllEmployees() throws Exception {
		
		when(employeeService.getAllEmp()).thenReturn(List.of(vaibhav(), amit()));

		
		mockMvc.perform(get("/employees").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].first_name").value("Vaibhav"))
				.andExpect(jsonPath("$[1].first_name").value("Amit"));

		verify(employeeService).getAllEmp();
	}

	@Test
	void shouldReturnEmployeesOfStateWhenStateParamIsGiven() throws Exception {
		
		when(employeeService.getEmpByState("Bihar")).thenReturn(List.of(amit()));

		
		mockMvc.perform(get("/employees").param("state", "Bihar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].state").value("Bihar"));

		verify(employeeService).getEmpByState("Bihar");
		verify(employeeService, never()).getAllEmp();
	}

	

	@Test
	void shouldReturnEmployeeWhenIdExists() throws Exception {
		
		when(employeeService.getEmplByid(175)).thenReturn(vaibhav());

		
		mockMvc.perform(get("/employees/175").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(175))
				.andExpect(jsonPath("$.first_name").value("Vaibhav"))
				.andExpect(jsonPath("$.last_name").value("Gupta"))
				.andExpect(jsonPath("$.age").value(23))
				.andExpect(jsonPath("$.salary").value(50000.0))
				.andExpect(jsonPath("$.city").value("Lucknow"))
				.andExpect(jsonPath("$.state").value("Uttar Pradesh"));

		verify(employeeService).getEmplByid(175);
	}

	@Test
	void shouldReturn404WhenEmployeeIdDoesNotExist() throws Exception {
		
		when(employeeService.getEmplByid(99))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"));

		
		mockMvc.perform(get("/employees/99"))
				.andExpect(status().isNotFound());
	}


	@Test
	void shouldCreateEmployeeAndReturn201() throws Exception {
		
		when(employeeService.addEmployee(any(Employee.class))).thenReturn(vaibhav());

		
		mockMvc.perform(post("/employees")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMPLOYEE_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(175))
				.andExpect(jsonPath("$.first_name").value("Vaibhav"));

		
		ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
		verify(employeeService).addEmployee(captor.capture());
		assertEquals(175, captor.getValue().getId());
		assertEquals("Vaibhav", captor.getValue().getFirst_name());
		assertEquals("Lucknow", captor.getValue().getCity());
	}

	@Test
	void shouldReturn409WhenEmployeeIdAlreadyExists() throws Exception {
		
		when(employeeService.addEmployee(any(Employee.class)))
				.thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Employee with this id already exist"));

		
		mockMvc.perform(post("/employees")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMPLOYEE_JSON))
				.andExpect(status().isConflict());
	}

	

	@Test
	void shouldUpdateEmployeeAndReturn200() throws Exception {
		
		Employee updated = new Employee(175, "Vaibhav", "Gupta", 24, 60000.0, "Delhi", "Delhi");
		when(employeeService.EmployeeUpdate(any(Employee.class), eq(175))).thenReturn(updated);

		
		mockMvc.perform(put("/employees/175")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMPLOYEE_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city").value("Delhi"))
				.andExpect(jsonPath("$.salary").value(60000.0));

		verify(employeeService).EmployeeUpdate(any(Employee.class), eq(175));
	}

	@Test
	void shouldReturn404WhenUpdatingMissingEmployee() throws Exception {
		
		when(employeeService.EmployeeUpdate(any(Employee.class), eq(99)))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"));

		
		mockMvc.perform(put("/employees/99")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(EMPLOYEE_JSON))
				.andExpect(status().isNotFound());
	}

	

	@Test
	void shouldDeleteEmployeeAndReturn204() throws Exception {
		
		mockMvc.perform(delete("/employees/175").with(csrf()))
				.andExpect(status().isNoContent());

		verify(employeeService).DeleteEmployee(175);
	}

	@Test
	void shouldReturn404WhenDeletingMissingEmployee() throws Exception {
		
		doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"))
				.when(employeeService).DeleteEmployee(99);

		
		mockMvc.perform(delete("/employees/99").with(csrf()))
				.andExpect(status().isNotFound());
	}
}
