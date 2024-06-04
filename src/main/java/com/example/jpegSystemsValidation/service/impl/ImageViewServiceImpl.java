package com.example.jpegSystemsValidation.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.ImageView;
import com.example.jpegSystemsValidation.model.User;
import com.example.jpegSystemsValidation.repo.ImageViewRepository;
import com.example.jpegSystemsValidation.service.ImageViewService;

@Service
public class ImageViewServiceImpl implements ImageViewService{
	
	@Autowired
	private ImageViewRepository imageViewRepository;

	@Override
	public void save(Image image, User user) {
		ImageView ImageView = new ImageView();
		
		ImageView.setUser(user);
		ImageView.setImage(image);
		
		imageViewRepository.save(ImageView);
		
	}

	@Override
	public List<ImageView> findImagesByUserId(Long userId) {
		return imageViewRepository.findByUserId(userId);
	}

	@Override
	public List<Object[]> findImagesAndImageViewByUserId(Long userId) {
		return imageViewRepository.findImagesAndImageViewByUserId(userId);
	}

}
