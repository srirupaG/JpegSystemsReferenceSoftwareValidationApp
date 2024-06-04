package com.example.jpegSystemsValidation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jpegSystemsValidation.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> { 
	
    
    User findByUsername(String username);
    

}
