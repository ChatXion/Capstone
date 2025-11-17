package com.example.demo.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import jakarta.persistence.CascadeType;


@Entity
@Table(name = "employees")
public class Employee extends User {
    
    private double ptoBalance;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    public Employee() {
        // this.ptoBalance = 0.0;
    }

    public double getPtoBalance() {
        return ptoBalance;
    }

    public void setPtoBalance(double ptoBalance) {
        this.ptoBalance = ptoBalance;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Timesheet> timesheets;

public List<Timesheet> getTimesheets() {
    return timesheets;
}

public void setTimesheets(List<Timesheet> timesheets) {
    this.timesheets = timesheets;
}
}