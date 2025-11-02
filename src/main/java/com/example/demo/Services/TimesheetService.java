package com.example.demo.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Timesheet;
import com.example.demo.Repositories.TimesheetEntryRepository;
import com.example.demo.Repositories.TimesheetRepository;

import jakarta.transaction.Transactional;

@Service
public class TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;

    public TimesheetService(TimesheetRepository timesheetRepository, 
                            TimesheetEntryRepository timesheetEntryRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetEntryRepository = timesheetEntryRepository;
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
}
