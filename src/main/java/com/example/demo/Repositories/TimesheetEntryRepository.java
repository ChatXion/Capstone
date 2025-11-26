package com.example.demo.Repositories;

import com.example.demo.Entities.TimesheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long> {
    TimesheetEntry findByTimesheetIdAndDateAndPaycodeId(Long timesheetId, LocalDate date, Long paycodeId);
}
