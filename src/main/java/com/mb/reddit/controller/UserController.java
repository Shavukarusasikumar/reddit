package com.mb.reddit.controller;

import com.mb.reddit.entity.User;
import com.mb.reddit.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class UserController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/user/login")
	public String showLoginPage() {
		return "login";
	}

	@GetMapping("/")
	public String home(Model model, Principal principal) {
		if (principal != null) {
			model.addAttribute("username", principal.getName());
		}
		return "home";
	}

	@GetMapping("/user/register")
	public String showRegisterPage() {
		return "register";
	}

	@PostMapping("/user/register")
	public String registerUser(
			@RequestParam String username,
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam(required = false) String bio,
			Model model) {
		if (userRepository.existsByUsername(username)) {
			model.addAttribute("errorMessage", "Username already taken");
			return "register";
		}

		if (userRepository.existsByEmail(email)) {
			model.addAttribute("errorMessage", "Email already registered");
			return "register";
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setBio(bio != null ? bio : "");

		userRepository.save(user);

		return "redirect:/user/login?registered=true";
	}
}
