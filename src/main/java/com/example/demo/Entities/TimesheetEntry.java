package com.example.demo.Entities;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "timesheet_entries")
public class TimesheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timesheet_entry_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "timesheet_id")
    private Timesheet timesheet;

    @ManyToOne
    @JoinColumn(name = "paycode_id")
    private PayCode payCode;

    private double hoursWorked;
    private LocalDate date;

    
    public TimesheetEntry() {
    }


    public Long getId() {
        return id;
    }


    public Timesheet getTimesheet() {
        return timesheet;
    }


    public PayCode getPayCode() {
        return payCode;
    }


    public double getHoursWorked() {
        return hoursWorked;
    }


    public LocalDate getDate() {
        return date;
    }
    
    
}
