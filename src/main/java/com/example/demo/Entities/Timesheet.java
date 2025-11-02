package com.example.demo.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "timesheets")
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timesheet_id_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL)
    private List<TimesheetEntry> entries = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    private int week;
    private String approvalStatus;
    private String approvedBy; // maybe make into Admin?
    private String rejectionReason;

    public Timesheet() {
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public List<TimesheetEntry> getEntries() {
        return entries;
    }

    public Organization getOrganization() {
        return organization;
    }

    public int getWeek() {
        return week;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    
}