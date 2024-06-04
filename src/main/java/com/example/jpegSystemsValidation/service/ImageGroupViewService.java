package com.example.jpegSystemsValidation.service;

import java.util.List;

import com.example.jpegSystemsValidation.model.Image;
import com.example.jpegSystemsValidation.model.ImageGroupView;
import com.example.jpegSystemsValidation.model.Role;

public interface ImageGroupViewService {
	
	void save(Image image, Role roleGiven);
	
	List<ImageGroupView> findByImageId(Long imageId);

}
