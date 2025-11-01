package com.example.demo.Services;

import org.springframework.stereotype.Service;

import com.example.demo.Repositories.TimesheetRepository;

@Service
public class TimesheetService {
    private final TimesheetRepository timesheetRepository;

    public TimesheetService(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }


}
