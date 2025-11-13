package com.example.demo.Repositories;

import com.example.demo.Entities.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    
    List<Timesheet> findByEmployeeId(Long employeeId);
    
    List<Timesheet> findByApprovalStatus(String approvalStatus);
    
    @Query("SELECT t FROM Timesheet t WHERE t.employee.organization.id = :organizationId AND t.approvalStatus = :status")
    List<Timesheet> findByOrganizationIdAndApprovalStatus(
        @Param("organizationId") Long organizationId, 
        @Param("status") String status
    );
    
    @Query("SELECT t FROM Timesheet t WHERE t.employee.id = :employeeId AND t.approvalStatus = :status")
    List<Timesheet> findByEmployeeIdAndApprovalStatus(
        @Param("employeeId") Long employeeId, 
        @Param("status") String status
    );
}