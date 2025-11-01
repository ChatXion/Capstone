package com.example.demo.Services;

import com.example.demo.Repositories.TimesheetRepository;

public class TimesheetService {
    private final TimesheetRepository timesheetRepository;

    public TimesheetService(TimesheetRepository timesheetRepository) {
        this.timesheetRepository = timesheetRepository;
    }


}
