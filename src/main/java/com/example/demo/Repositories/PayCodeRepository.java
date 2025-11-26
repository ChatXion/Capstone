package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.Entities.Paycode;
import java.util.List;

@Repository
public interface PayCodeRepository extends JpaRepository<Paycode, Long> {
    List<Paycode> findByOrganizationId(Long organizationId);
}