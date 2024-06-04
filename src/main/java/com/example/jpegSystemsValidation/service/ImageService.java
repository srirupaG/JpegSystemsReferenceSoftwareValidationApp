package com.example.jpegSystemsValidation.service;

import java.util.List;
import java.util.Optional;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.User;

public interface ImageService {
	

	Image saveImage(String filePath, String imagename, User user);
	
	List<Image> findImagesByUserId(Long userId);
	
	Optional<Image> findByImageId(Long imageId);

	void hideImage(Long imageId);

	Image saveOrUpdateImage(Long id, String imageName, String filePath, User createdBy, boolean isHidden);
	
	List<Image> findAllVisibleImages();
	

}
