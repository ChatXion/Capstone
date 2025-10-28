package com.example.demo.Repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.example.demo.Entities.User;

@NoRepositoryBean
public interface UserRepository extends CrudRepository<User, Long>{
}
