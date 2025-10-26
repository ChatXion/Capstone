package com.example.demo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String userType;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    public User(){
        //
    }

    public static User register(String firstName, String lastName, String email, String password){
        //check if email and password are used from database
        //return new user object with data if exists
        return new User();
    }

    public static User login(String email, String password){
        //check if email and password combo exist in database
        //return new user object with data if exists
        return new User();
    }

    public void logout(){
        //log user out
    }
}
