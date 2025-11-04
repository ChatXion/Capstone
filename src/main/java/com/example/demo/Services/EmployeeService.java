package com.example.demo.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Repositories.EmployeeRepository;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Optional<Employee> getEmployee(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public double getEmployeePTOBalance(Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        return employeeOpt.map(Employee::getPtoBalance).orElse(0.0);
    }

    @Transactional
    public Employee updatePTOBalance(Long employeeId, double newBalance) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setPtoBalance(newBalance);
            return employeeRepository.save(employee);
        }
        return null;
    }

    @Transactional
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Transactional
public List<Timesheet> findAllTimesheets(Long employeeId) {
    Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
    if (employeeOpt.isPresent()) {
        Employee employee = employeeOpt.get();
        return employee.getTimesheets(); // Assumes Employee has a getTimesheets() method
    }
    return new ArrayList<>();
}

@Transactional
public Optional<Timesheet> getTimesheet(Long employeeId, Long timesheetId) {
    Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
    if (employeeOpt.isPresent()) {
        Employee employee = employeeOpt.get();
        if (employee.getTimesheets() != null) {
            return employee.getTimesheets().stream()
                .filter(ts -> ts.getId().equals(timesheetId))
                .findFirst();
        }
    }
    return Optional.empty();
}
}