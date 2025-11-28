package com.example.demo.Repositories;

import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Admin;

@Repository
public interface AdminRepository extends UserRepository<Admin> {
}