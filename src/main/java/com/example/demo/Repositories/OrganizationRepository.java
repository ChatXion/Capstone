package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Organization;

@Repository
public interface OrganizationRepository extends CrudRepository<Organization, Long>{
}
