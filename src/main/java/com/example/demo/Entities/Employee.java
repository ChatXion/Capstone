package com.example.demo.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee extends User{
    
    @OneToMany(mappedBy = "employee")
    private List<Timesheet> timesheets = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    private double ptoBalance;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<PTORequest> ptoRequests;
    
    public Employee() {
    }

    public Employee(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }

    public List<Timesheet> getTimesheets() {
        return timesheets;
    }

    public Role getRole() {
        return role;
    }

    public double getPtoBalance() {
        return ptoBalance;
    }

}
