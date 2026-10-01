package com.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

	@Autowired
	private EmployeeService employeeservice;
	
	@GetMapping
	public List<Employee> getallEmployee(@RequestParam(required=false) String state){
		if(state!=null) {
			return this.employeeservice.getEmpByState(state);
		}
		return this.employeeservice.getAllEmp();
	}
	
	@GetMapping("/{id}")
	public Employee getEmpBYID(@PathVariable int id) {
		Employee e=this.employeeservice.getEmplByid(id);
		return e;
	}
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Employee AddEmployee(@RequestBody Employee e) {
		Employee e1 = this.employeeservice.addEmployee(e);
		return e1;
	}
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void Delete(@PathVariable int id) {
		this.employeeservice.DeleteEmployee(id);
	}
	@PutMapping("/{id}")
	public Employee UpdateEmployee(@RequestBody Employee e,@PathVariable int id) {
		return this.employeeservice.EmployeeUpdate(e, id);
	}
	
}
