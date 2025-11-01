package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Employee;

@Repository
public interface EmployeeRepository extends UserRepository<Employee> {
    // Optional<Employee> findByEmail(String email);
}