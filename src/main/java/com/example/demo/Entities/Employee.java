package com.example.demo.Entities;

import java.util.ArrayList;
import java.util.List;

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

    
}
