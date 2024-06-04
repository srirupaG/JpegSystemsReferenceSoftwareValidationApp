package com.example.jpegSystemsValidation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jpegSystemsValidation.model.ImageGroupView;

@Repository
public interface ImageGroupViewRepository extends JpaRepository<ImageGroupView, Long>{
	
	List<ImageGroupView> findByImageId(Long imageId);

}
