package com.example.demo.Repositories;

import com.example.demo.Entities.PTORequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PTORequestRepository extends JpaRepository<PTORequest, Long> {
    
    List<PTORequest> findByEmployeeId(Long employeeId);
    
    List<PTORequest> findByApprovalStatus(String approvalStatus);
    
    @Query("SELECT p FROM PTORequest p WHERE p.employee.organization.id = :organizationId AND p.approvalStatus = :status")
    List<PTORequest> findByOrganizationIdAndApprovalStatus(
        @Param("organizationId") Long organizationId, 
        @Param("status") String status
    );
    
    @Query("SELECT p FROM PTORequest p WHERE p.employee.id = :employeeId AND p.approvalStatus = :status")
    List<PTORequest> findByEmployeeIdAndApprovalStatus(
        @Param("employeeId") Long employeeId, 
        @Param("status") String status
    );
}