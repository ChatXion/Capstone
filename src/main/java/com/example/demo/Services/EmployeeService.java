package com.example.demo.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Repositories.EmployeeRepository;

import jakarta.transaction.Transactional;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final TimesheetService timesheetService; 

    public EmployeeService(EmployeeRepository employeeRepository, 
                           TimesheetService timesheetService) {
        this.employeeRepository = employeeRepository;
        this.timesheetService = timesheetService;
    }

    @Transactional
    public Optional<Employee> getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }
    
    
    @Transactional
    public Optional<Organization> getEmployeeOrganization(Long employeeId) {
        // Fetch the employee, then safely map the Organization field (which is EAGER by default for ManyToOne)
        return employeeRepository.findById(employeeId)
            .map(Employee::getOrganization); 
    }

    public List<Timesheet> findAllTimesheets(Long employeeId) {
        return timesheetService.findAllTimesheetsByEmployeeId(employeeId); 
    }

    @Transactional
    public Optional<Timesheet> getTimesheet(Long employeeId, Long timesheetId) {
        
        Optional<Timesheet> timesheetOpt = timesheetService.getTimesheetWithEntries(timesheetId);
        
        if (timesheetOpt.isEmpty()) {
            return Optional.empty(); // Not found
        }
        
        Timesheet timesheet = timesheetOpt.get();
        
        // Check if logged in user owns the timesheet
        if (timesheet.getEmployee() != null && timesheet.getEmployee().getId().equals(employeeId)) {
            return timesheetOpt; // Authorized
        }
        
        // User is not authorized to view this timesheet
        return Optional.empty(); 
    }
    
    
}
