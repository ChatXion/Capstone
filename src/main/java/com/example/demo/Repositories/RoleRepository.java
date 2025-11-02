package com.example.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    List<Role> findByEmployees_Id(Long employeeId);
}
