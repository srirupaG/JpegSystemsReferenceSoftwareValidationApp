package com.example.jpegSystemsValidation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.jpegSystemsValidation.model.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>{
	
	@Query("SELECT i FROM Image i WHERE i.createdBy.id = ?1")
    List<Image> findByUserId(Long userId);
	
	@Query("SELECT i FROM Image i WHERE i.isHidden = false")
    List<Image> findAllVisibleImages();
	
}
