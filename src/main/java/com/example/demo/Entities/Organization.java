package com.example.demo.Entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_seq")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "organization")
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Admin> admins = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<PayCode> payCodes = new ArrayList<>();

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<Role> roles = new ArrayList<>();
    
    public Organization() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Admin> getAdmins() {
        return admins;
    }

    public List<PayCode> getPayCodes() {
        return payCodes;
    }

    public List<Role> getRoles() {
        return roles;
    }
    
    
}
