package com.example.demo.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User{
    
    public Admin(){
    }

    public Admin(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
    }
}
