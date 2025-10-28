package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Timesheet;

@Repository
public interface TimesheetRepository extends CrudRepository<Timesheet, Long>{
}
