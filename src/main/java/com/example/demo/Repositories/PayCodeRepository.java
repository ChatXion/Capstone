package com.example.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.PayCode;

@Repository
public interface PayCodeRepository extends JpaRepository<PayCode, Long>{
}
