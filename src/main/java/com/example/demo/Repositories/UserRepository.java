package com.example.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.demo.Entities.User;

@NoRepositoryBean
public interface UserRepository<T extends User> extends JpaRepository<T, Long>{
    
    Optional<T> findByEmail(String email);
}
