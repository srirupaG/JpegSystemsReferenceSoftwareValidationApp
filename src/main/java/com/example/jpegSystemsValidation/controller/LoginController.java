package com.example.jpegSystemsValidation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LoginController {
	
	/** 
	 * Starting of the app
	 *  */
	@GetMapping("/")
	public String welcomeToIndex() {
		return "index";
	}

	
	/** 
	 * Redirect to the login page
	 *  */
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	
	/** 
	 * Redirect to index page
	 * */
	@GetMapping("/indexPage")
	public String indexPageRedirection() {
		return "index";
	}
	
}
