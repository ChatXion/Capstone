package com.example.demo.Repositories;

import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Employee;

@Repository
public interface EmployeeRepository extends UserRepository<Employee> {
}