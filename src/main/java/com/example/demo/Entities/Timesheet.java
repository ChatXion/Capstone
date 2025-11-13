package com.example.demo.Entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
public class Timesheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @Column(name = "week")
    private Integer week;
    
    @Column(name = "approval_status")
    private String approvalStatus;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimesheetEntry> entries = new ArrayList<>();
    
    // Constructors
    public Timesheet() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    public Integer getWeek() {
        return week;
    }
    
    public void setWeek(Integer week) {
        this.week = week;
    }
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public List<TimesheetEntry> getEntries() {
        return entries;
    }
    
    public void setEntries(List<TimesheetEntry> entries) {
        this.entries = entries;
    }
    
    // Helper methods
    public double getTotalHours() {
        if (entries == null || entries.isEmpty()) {
            return 0.0;
        }
        return entries.stream()
                     .mapToDouble(TimesheetEntry::getHoursWorked)
                     .sum();
    }
    
    public LocalDate getStartDate() {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        return entries.stream()
                     .map(TimesheetEntry::getDate)
                     .filter(date -> date != null)
                     .min(LocalDate::compareTo)
                     .orElse(null);
    }
    
    public LocalDate getEndDate() {
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        return entries.stream()
                     .map(TimesheetEntry::getDate)
                     .filter(date -> date != null)
                     .max(LocalDate::compareTo)
                     .orElse(null);
    }
    
    public LocalDate getSubmittedDate() {
        // Assuming submitted date is the day after the end date
        LocalDate endDate = getEndDate();
        return endDate != null ? endDate.plusDays(1) : null;
    }
    
    public String getNotes() {
        // Generate notes based on total hours
        double totalHours = getTotalHours();
        if (totalHours > 40) {
            return String.format("Includes %.1f hours overtime", totalHours - 40);
        } else if (totalHours < 40 && totalHours > 0) {
            return String.format("Took %.1f hours off", 40 - totalHours);
        } else if (totalHours == 40) {
            return "Standard week";
        } else {
            return "Regular work week";
        }
    }
}