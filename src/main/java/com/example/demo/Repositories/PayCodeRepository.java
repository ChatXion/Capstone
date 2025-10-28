package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.PayCode;

@Repository
public interface PayCodeRepository extends CrudRepository<PayCode, Long>{
}
