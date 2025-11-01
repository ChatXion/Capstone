package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.demo.Entities.User;

@NoRepositoryBean
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findById(Long id);

    
    Optional<User> findByEmail(String email);
}
