package com.example.demo.Entities;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "paycodes")
public class PayCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paycode_id_seq")
    private Long id;

    private String name;
    private String code;
    private String description;
    private BigDecimal hourlyRate;
    
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    public PayCode() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public Organization getOrganization() {
        return organization;
    }

    
}
