package com.example.jpegSystemsValidation.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.ImageGroupView;
import com.example.jpegSystemsValidation.model.Role;
import com.example.jpegSystemsValidation.repo.ImageGroupViewRepository;
import com.example.jpegSystemsValidation.service.ImageGroupViewService;

@Service
public class ImageGroupViewServiceImpl implements ImageGroupViewService{
	
	@Autowired
	private ImageGroupViewRepository imageGroupViewRepository;

	@Override
	public void save(Image image, Role roleGiven) {
		
		ImageGroupView imageGroupView = new ImageGroupView();
		
		imageGroupView.setImage(image);
		imageGroupView.setGroupRole(roleGiven);
		
		imageGroupViewRepository.save(imageGroupView);
		
	}

	@Override
	public List<ImageGroupView> findByImageId(Long imageId) {
		return imageGroupViewRepository.findByImageId(imageId);
	}

}
