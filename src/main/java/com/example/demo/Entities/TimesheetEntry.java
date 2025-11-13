package com.example.demo.Entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "timesheet_entries")
public class TimesheetEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;
    
    @ManyToOne
    @JoinColumn(name = "paycode_id")
    private Paycode paycode;
    
    @Column(name = "date")
    private LocalDate date;
    
    @Column(name = "hours_worked")
    private Double hoursWorked;
    
    // Constructors
    public TimesheetEntry() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Timesheet getTimesheet() {
        return timesheet;
    }
    
    public void setTimesheet(Timesheet timesheet) {
        this.timesheet = timesheet;
    }
    
    public Paycode getPaycode() {
        return paycode;
    }
    
    public void setPaycode(Paycode paycode) {
        this.paycode = paycode;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Double getHoursWorked() {
        return hoursWorked;
    }
    
    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }
}