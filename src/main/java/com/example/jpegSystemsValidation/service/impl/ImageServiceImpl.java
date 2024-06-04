package com.example.jpegSystemsValidation.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.User;
import com.example.jpegSystemsValidation.repo.ImageRepository;
import com.example.jpegSystemsValidation.service.ImageService;

@Service
public class ImageServiceImpl implements ImageService{
	
	@Autowired
	private ImageRepository imageRepository;
	
    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }
	

	@Override
	public Image saveImage(String filePath, String imageName, User user) {
		
		Image image = new Image();
		image.setImageName(imageName);
		image.setCreatedBy(user);
		image.setFilePath(filePath);
		image.setHidden(false);
		
		return imageRepository.save(image);
	}


	@Override
	public List<Image> findImagesByUserId(Long userId) {
		return imageRepository.findByUserId(userId);
	}


	@Override
	public Optional<Image> findByImageId(Long imageId) {
		Optional<Image> optionalImage = imageRepository.findById(imageId);
		
		return optionalImage;
	}


	@Override
	public void hideImage(Long imageId) {
		
		Image image = new Image();
		image.setId(imageId);
		image.setHidden(true);
		imageRepository.save(image);
		
	}


	@Override
	public Image saveOrUpdateImage(Long id, String imageName, String filePath, User createdBy, boolean isHidden) {
		
		Image image = new Image();
		image.setId(id);
		image.setImageName(imageName);
		image.setCreatedBy(createdBy);
		image.setFilePath(filePath);
		
		if(isHidden==false) {
			image.setHidden(true);
		}else {
			image.setHidden(false);
		}
		
		return imageRepository.save(image);
		
	}


	@Override
	public List<Image> findAllVisibleImages() {
		return imageRepository.findAllVisibleImages();
	}
	
//	public List<Image> findAll(){
//		return imageRepository.findAll();
//	}



}
