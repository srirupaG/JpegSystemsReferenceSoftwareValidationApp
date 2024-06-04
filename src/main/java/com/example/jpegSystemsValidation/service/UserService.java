package com.example.jpegSystemsValidation.service;

import com.example.jpegSystemsValidation.request.UserDTO;
import com.example.jpegSystemsValidation.response.BaseResponse;

public interface UserService {
	
	BaseResponse registerAccount(UserDTO userDTO);

}
