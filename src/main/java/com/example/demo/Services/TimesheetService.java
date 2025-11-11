package com.example.demo.Services;

import java.time.LocalDate;
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
import com.example.demo.Repositories.TimesheetRepository;

import jakarta.transaction.Transactional;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final EmployeeRepository employeeRepository;
    private final PayCodeRepository payCodeRepository;

    public TimesheetService(TimesheetRepository timesheetRepository, 
                            TimesheetEntryRepository timesheetEntryRepository,
                            EmployeeRepository employeeRepository,
                            PayCodeRepository payCodeRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetEntryRepository = timesheetEntryRepository;
        this.employeeRepository = employeeRepository;
        this.payCodeRepository = payCodeRepository;
    }

    
    @Transactional
    public List<Timesheet> findAllTimesheetsByEmployeeId(Long employeeId) {
        return timesheetRepository.findByEmployeeId(employeeId); 
    }

    @Transactional
    public Optional<Timesheet> getTimesheetWithEntries(Long timesheetId) {
        Optional<Timesheet> timesheetOpt = timesheetRepository.findById(timesheetId);
        
        // For handling lazy loading
        if (timesheetOpt.isPresent()) {
            // Accessing entries forces the load within this transaction scope
            timesheetOpt.get().getEntries().size(); 
        }
        
        return timesheetOpt;
    }
    
    @Transactional
    public void createTimesheetEntry(Long employeeId, Integer week, LocalDate date, 
                                     Double hours, Long payCodeId) {
        // Get the employee
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }
        
        Employee employee = employeeOpt.get();
        
        // Get the pay code
        Optional<PayCode> payCodeOpt = payCodeRepository.findById(payCodeId);
        if (payCodeOpt.isEmpty()) {
            throw new IllegalArgumentException("Pay code not found with ID: " + payCodeId);
        }
        
        PayCode payCode = payCodeOpt.get();
        
        // Find or create timesheet for this employee and week
        Timesheet timesheet = findOrCreateTimesheet(employee, week);
        
        // Create the timesheet entry
        TimesheetEntry entry = new TimesheetEntry();
        entry.setDate(date);
        entry.setHoursWorked(hours);
        entry.setPayCode(payCode);
        entry.setTimesheet(timesheet);
        
        // Save the entry
        timesheetEntryRepository.save(entry);
    }
    
    @Transactional
    public Timesheet findOrCreateTimesheet(Employee employee, Integer week) {
        // Try to find existing timesheet for this employee and week
        List<Timesheet> timesheets = timesheetRepository.findByEmployeeId(employee.getId());
        
        for (Timesheet ts : timesheets) {
            if (ts.getWeek() == week) {
                return ts;
            }
        }
        
        // If not found, create a new timesheet
        Timesheet newTimesheet = new Timesheet();
        newTimesheet.setEmployee(employee);
        newTimesheet.setWeek(week);
        newTimesheet.setApprovalStatus("pending");
        newTimesheet.setOrganization(employee.getOrganization());
        
        return timesheetRepository.save(newTimesheet);
    }
    
    @Transactional
    public void approveTimesheet(Long timesheetId, String approvedBy) {
        Optional<Timesheet> timesheetOpt = timesheetRepository.findById(timesheetId);
        
        if (timesheetOpt.isPresent()) {
            Timesheet timesheet = timesheetOpt.get();
            
            // Only approve if currently pending
            if ("pending".equals(timesheet.getApprovalStatus())) {
                timesheet.setApprovalStatus("approved");
                timesheet.setApprovedBy(approvedBy);
                timesheet.setRejectionReason(null); // Clear any previous rejection reason
                
                timesheetRepository.save(timesheet);
            }
        }
    }
    
    @Transactional
    public void denyTimesheet(Long timesheetId, String rejectionReason, String deniedBy) {
        Optional<Timesheet> timesheetOpt = timesheetRepository.findById(timesheetId);
        
        if (timesheetOpt.isPresent()) {
            Timesheet timesheet = timesheetOpt.get();
            
            // Only deny if currently pending
            if ("pending".equals(timesheet.getApprovalStatus())) {
                timesheet.setApprovalStatus("rejected");
                timesheet.setRejectionReason(rejectionReason);
                timesheet.setApprovedBy(deniedBy); // Track who denied it
                
                timesheetRepository.save(timesheet);
            }
        }
    }
}