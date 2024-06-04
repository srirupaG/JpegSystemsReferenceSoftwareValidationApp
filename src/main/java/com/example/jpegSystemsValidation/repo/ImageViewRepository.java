package com.example.jpegSystemsValidation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.jpegSystemsValidation.model.ImageView;

@Repository
public interface ImageViewRepository extends JpaRepository<ImageView, Long>{
	
	@Query("SELECT i FROM ImageView i WHERE i.user.id = ?1")
    List<ImageView> findByUserId(Long userId);
	
	@Query("SELECT i.id, i.imageName, i.filePath, iv.id FROM Image i JOIN ImageView iv ON i.id = iv.image.id WHERE iv.user.id = ?1")
    List<Object[]> findImagesAndImageViewByUserId(Long userId);
	
}
