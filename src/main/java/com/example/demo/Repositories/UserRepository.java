package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.demo.Entities.User;

@NoRepositoryBean
public interface UserRepository extends CrudRepository<User, Long>{

    Optional<User> findById(Long id);

    
    Optional<User> findByEmail(String email);
}
