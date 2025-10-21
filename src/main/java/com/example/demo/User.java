package com.example.demo;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String userType;
    // private Organization organization;

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
