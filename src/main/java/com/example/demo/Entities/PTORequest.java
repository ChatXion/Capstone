package com.example.demo.Entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pto_requests")
public class PTORequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "approval_status")
    private String approvalStatus;

    public PTORequest() {
    }

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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    // Helper methods for calculated fields
    public Double getHoursRequested() {
        if (startDate != null && endDate != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            return days * 8.0; // Assuming 8 hours per day
        }
        return 0.0;
    }
    
    public LocalDate getSubmittedDate() {
        // Since we don't have this in the DB, return the start date minus some days as estimate
        // Or you could return null and handle it in the controller
        return startDate != null ? startDate.minusDays(7) : LocalDate.now();
    }
    
    public String getRequestType() {
        // Since we don't have this in DB, return a default
        return "PTO";
    }
    
    public String getReason() {
        // Since we don't have this in DB, return empty string
        return "";
    }
    
    public String getApprovedBy() {
        // Since we don't have this in DB, return empty string
        return "";
    }
    
    public String getRejectionReason() {
        // Since we don't have this in DB, return empty string
        return "";
    }
}