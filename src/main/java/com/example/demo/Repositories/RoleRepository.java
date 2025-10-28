package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entities.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long>{
}
