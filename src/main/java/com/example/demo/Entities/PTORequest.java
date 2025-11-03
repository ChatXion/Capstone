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
@Table(name = "pto_requests")
public class PTORequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pto_request_id_seq")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String approvalStatus;


    public Long getId() {
        return id;
    }
    public Employee getEmployee() {
        return employee;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public String getApprovalStatus() {
        return approvalStatus;
    }

    
}
