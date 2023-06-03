package com.challamani.filters.controller;

import com.challamani.filters.model.Employee;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestController
public class EmployeeController {
    private static final List<Employee>  employees = new LinkedList<>(
    Arrays.asList(
            new Employee(1, "I am the King", "Technology"),
            new Employee(2, "I am the Legend", "Risk"),
            new Employee(3, "I am Everything", "HR"),
            new Employee(4, "I am Invisible", "Quality"),
            new Employee(5, "I am Inevitable", "Security")
    ));

    @GetMapping("/employees")
    public List<Employee> getEmployees() {
        return employees;
    }

    @DeleteMapping("/employees/{id}")
    public Employee removeEmployee(@PathVariable(name = "id") Integer id) {

        Employee emp = employees.stream().filter(employee -> employee.id() == id)
                .findFirst()
                .orElseThrow(() -> new HttpClientErrorException(HttpStatusCode.valueOf(404), "Not found!"));

        employees.remove(emp);
        return emp;
    }
}
