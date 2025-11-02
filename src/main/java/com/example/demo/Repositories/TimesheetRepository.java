package com.example.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Timesheet;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long>{
    List<Timesheet> findByEmployeeId(Long employeeId);
}
