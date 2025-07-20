package com.mb.reddit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class UserController {

	@GetMapping("/user/login")
	public String showLoginPage() {
		return "login";
	}

	@GetMapping("/")
	public String home(Model model, Principal principal) {
		if (principal != null) {
			model.addAttribute("username", principal.getName());
		}
		return "home-temp";
	}
}
