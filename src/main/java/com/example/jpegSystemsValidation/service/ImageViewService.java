package com.example.jpegSystemsValidation.service;

import java.util.List;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.ImageView;
import com.example.jpegSystemsValidation.model.User;

public interface ImageViewService {
	
	void save(Image image, User user);
	
	List<ImageView> findImagesByUserId(Long userId);
	
	List<Object[]> findImagesAndImageViewByUserId(Long userId);

}
