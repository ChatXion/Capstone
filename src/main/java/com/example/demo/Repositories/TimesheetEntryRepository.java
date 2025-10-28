package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.TimesheetEntry;

@Repository
public interface TimesheetEntryRepository extends CrudRepository<TimesheetEntry, Long>{
}
