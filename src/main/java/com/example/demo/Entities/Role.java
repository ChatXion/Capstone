package com.example.demo.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_id_seq")
    private Long id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "role")
    private List<Employee> employees = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "role_Paycode",                                  // Name of the join table
        joinColumns = @JoinColumn(name = "role_id"),            // Foreign key for Role
        inverseJoinColumns = @JoinColumn(name = "Paycode_id")   // Foreign key for Paycode
    )
    private List<Paycode> Paycodes = new ArrayList<>();

    public Role() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Organization getOrganization() {
        return organization;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Paycode> getPaycodes() {
        return Paycodes;
    }

    
}
