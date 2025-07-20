package com.mb.reddit.controller;

import com.mb.reddit.entity.User;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.UserService;
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
    private final UserService userService;

	public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
						  UserService userService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.userService = userService;
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

	@GetMapping("/user")
	public String getUserProfilePage(Model model){
		User user = new User();//TODO :current user is null so just checking
		user.setId(1L);
		user.setUsername("Sanjeet Ji");
		user.setProfilePicture("https://i.pravatar.cc/100?img=5%22");
		model.addAttribute("user", user);

		return "fragments/user-profile-middle";
	}
}
