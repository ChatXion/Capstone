package com.example.demo.Services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PayCode;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Entities.TimesheetEntry;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.PayCodeRepository;
import com.example.demo.Repositories.TimesheetEntryRepository;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final PayCodeRepository payCodeRepository;

    public EmployeeService(EmployeeRepository employeeRepository, 
                          TimesheetEntryRepository timesheetEntryRepository,
                          PayCodeRepository payCodeRepository) {
        this.employeeRepository = employeeRepository;
        this.timesheetEntryRepository = timesheetEntryRepository;
        this.payCodeRepository = payCodeRepository;
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
            return employee.getTimesheets();
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

    @Transactional
    public void updateTimesheetEntry(Long entryId, LocalDate date, double hoursWorked, Long payCodeId) {
        Optional<TimesheetEntry> entryOpt = timesheetEntryRepository.findById(entryId);
        Optional<PayCode> payCodeOpt = payCodeRepository.findById(payCodeId);
        
        if (entryOpt.isPresent() && payCodeOpt.isPresent()) {
            TimesheetEntry entry = entryOpt.get();
            
            // Only allow editing if the timesheet is pending
            if (entry.getTimesheet() != null && "pending".equals(entry.getTimesheet().getApprovalStatus())) {
                entry.setDate(date);
                entry.setHoursWorked(hoursWorked);
                entry.setPayCode(payCodeOpt.get());
                timesheetEntryRepository.save(entry);
            }
        }
    }
}