package com.example.demo.Services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PTORequest;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.PTORequestRepository;

import jakarta.transaction.Transactional;

@Service
public class PTOService {
    
    private final PTORequestRepository ptoRequestRepository;
    private final EmployeeRepository employeeRepository;
    
    public PTOService(PTORequestRepository ptoRequestRepository, 
                     EmployeeRepository employeeRepository) {
        this.ptoRequestRepository = ptoRequestRepository;
        this.employeeRepository = employeeRepository;
    }
    
    @Transactional
    public List<PTORequest> findAllPTORequestsByEmployeeId(Long employeeId) {
        return ptoRequestRepository.findByEmployeeId(employeeId);
    }
    
    @Transactional
    public Optional<PTORequest> getPTORequest(Long employeeId, Long requestId) {
        Optional<PTORequest> requestOpt = ptoRequestRepository.findById(requestId);
        
        // Verify the request belongs to this employee
        if (requestOpt.isPresent() && requestOpt.get().getEmployee().getId().equals(employeeId)) {
            return requestOpt;
        }
        
        return Optional.empty();
    }
    
    @Transactional
    public void createPTORequest(Long employeeId, String requestType, LocalDate startDate, 
                                LocalDate endDate, String reason) {
        // Get the employee
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }
        
        Employee employee = employeeOpt.get();
        
        // Calculate hours requested (assuming 8 hours per day)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 to include both days
        double hoursRequested = daysBetween * 8.0;
        
        // Check if employee has enough PTO balance
        if (hoursRequested > employee.getPtoBalance()) {
            throw new IllegalArgumentException("Insufficient PTO balance. Requested: " + hoursRequested + 
                                             " hours, Available: " + employee.getPtoBalance() + " hours");
        }
        
        // Create the PTO request
        PTORequest request = new PTORequest();
        request.setEmployee(employee);
        request.setRequestType(requestType);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setReason(reason);
        request.setHoursRequested(hoursRequested);
        request.setSubmittedDate(LocalDate.now());
        request.setApprovalStatus("pending");
        
        ptoRequestRepository.save(request);
    }
    
    @Transactional
    public void updatePTORequest(Long requestId, String requestType, LocalDate startDate, 
                                LocalDate endDate, String reason, Long employeeId) {
        Optional<PTORequest> requestOpt = ptoRequestRepository.findById(requestId);
        
        if (requestOpt.isPresent()) {
            PTORequest request = requestOpt.get();
            
            // Only allow editing if pending and belongs to employee
            if ("pending".equals(request.getApprovalStatus()) && 
                request.getEmployee().getId().equals(employeeId)) {
                
                // Recalculate hours
                long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                double hoursRequested = daysBetween * 8.0;
                
                // Check balance
                if (hoursRequested > request.getEmployee().getPtoBalance()) {
                    throw new IllegalArgumentException("Insufficient PTO balance. Requested: " + hoursRequested + 
                                                     " hours, Available: " + request.getEmployee().getPtoBalance() + " hours");
                }
                
                request.setRequestType(requestType);
                request.setStartDate(startDate);
                request.setEndDate(endDate);
                request.setReason(reason);
                request.setHoursRequested(hoursRequested);
                
                ptoRequestRepository.save(request);
            }
        }
    }
    
    @Transactional
    public void deletePTORequest(Long requestId, Long employeeId) {
        Optional<PTORequest> requestOpt = ptoRequestRepository.findById(requestId);
        
        if (requestOpt.isPresent()) {
            PTORequest request = requestOpt.get();
            
            // Only allow deleting if pending and belongs to employee
            if ("pending".equals(request.getApprovalStatus()) && 
                request.getEmployee().getId().equals(employeeId)) {
                ptoRequestRepository.delete(request);
            }
        }
    }
}