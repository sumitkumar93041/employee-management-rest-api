package com.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepo employeerepo;
	
	public List<Employee> getAllEmp(){
		return (List<Employee>) this.employeerepo.findAll();
	}
	
	public List<Employee> getEmpByState(String state){
		return this.employeerepo.findByState(state);
	}
	public Employee getEmplByid(int id) {
	    return employeerepo.findById(id)
	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee Not Found"));
	}
	
	public Employee addEmployee(Employee e) {
		if(employeerepo.existsById(e.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,"Employee with this id already exist");
		}
		Employee save = this.employeerepo.save(e);
		return save;
	}
	
	public void DeleteEmployee(int eid) {
		if(!employeerepo.existsById(eid)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found");
		}
		this.employeerepo.deleteById(eid);
	}
	
	public Employee EmployeeUpdate(Employee e, int eid) {
		if(!employeerepo.existsById(eid)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Employee Not Found");
		}
		e.setId(eid);
		return this.employeerepo.save(e);
	}
	
}
