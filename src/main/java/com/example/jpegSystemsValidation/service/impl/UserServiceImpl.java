package com.example.jpegSystemsValidation.service.impl;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.example.jpegSystemsValidation.model.Role;
import com.example.jpegSystemsValidation.model.User;
import com.example.jpegSystemsValidation.repo.RoleRepository;
import com.example.jpegSystemsValidation.repo.UserRepository;
import com.example.jpegSystemsValidation.request.UserDTO;
import com.example.jpegSystemsValidation.response.BaseResponse;
import com.example.jpegSystemsValidation.service.UserService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	
	private final RoleRepository roleRepository = null;
	
	private final UserRepository userRepository = null;
	
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Override
	public BaseResponse registerAccount(UserDTO userDTO) {
		return null;
	}
	
	private void validateAccount(UserDTO userDTO){
        if(ObjectUtils.isEmpty(userDTO)){
        }

        List<String> roles = roleRepository.findAll().stream().map(Role::getName).toList();

        if(!roles.contains(userDTO.getRole())){
        }

        User user = userRepository.findByUsername(userDTO.getUsername());

        if(!ObjectUtils.isEmpty(user)){
        }

    }

}
