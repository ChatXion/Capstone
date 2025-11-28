package com.example.demo.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{
    List<Role> findByEmployees_Id(Long employeeId);

    /**
     * Retrieves a Role entity by its name, scoped by the specific Organization.
     * This query is essential for handling non-unique role names across organizations.
     */
    Optional<Role> findByNameAndOrganization(String name, Organization organization);
    
    // Used by RoleService.findAll()
    List<Role> findAllByOrganization(Organization organization);
}
