package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Organization;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>{
    /**
     * Finds an Organization entity based on the ID of one of its associated Employees.
     * @param employeeId The primary key ID of the Employee.
     * @return An Optional containing the Organization if found.
     */
    Optional<Organization> findByEmployees_Id(Long employeeId);
    
    /**
     * Finds an Organization entity based on the ID of one of its associated Admins.
     * @param adminId The primary key ID of the Admin.
     * @return An Optional containing the Organization if found.
     */
    Optional<Organization> findByAdmins_Id(Long adminId);
}
