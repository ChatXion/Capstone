package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.TimesheetEntry;

@Repository
public interface TimesheetEntryRepository extends JpaRepository<TimesheetEntry, Long>{
}
