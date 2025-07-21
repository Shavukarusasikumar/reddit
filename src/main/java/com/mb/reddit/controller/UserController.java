package com.mb.reddit.controller;

import com.mb.reddit.entity.User;
import com.mb.reddit.repository.UserRepository;
import com.mb.reddit.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.mb.reddit.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/user")
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

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if(principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "home";
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

	@PostMapping("/register")
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

		User registeredUser = userService.registerUser(username, email, password, bio);

		Authentication authentication = new UsernamePasswordAuthenticationToken(
				registeredUser.getUsername(),
				null,
				Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return "redirect:/";
	}


    @PostMapping("/join-community/{id}")
    @ResponseBody
    public ResponseEntity<String> joinCommunity(@PathVariable("id") Long communityId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        try {
            userService.adduserToCommunityByCommunityId(communityId);
            return ResponseEntity.ok("Joined community");
        } catch(Exception e) {
            return ResponseEntity.badRequest().body("Error joining community");
        }
    }


    @PostMapping("/leave-community/{id}")
    @ResponseBody
    public ResponseEntity<String> leaveCommunity(@PathVariable("id") Long communityId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        userService.removeUserFromCommunityByCommunityId(communityId);

        return ResponseEntity.ok("Removed  community");
    }


	@GetMapping("/user")
	public String getUserProfilePage(Model model){
		User user = userService.getCurrentUser();

		/*User user = new User();//TODO :current user is null so just checking
		user.setId(1L);
		user.setUsername("Sanjeet Ji");
		user.setProfilePicture("https://i.pravatar.cc/100?img=5%22");
		user.setBio("User's Bio");*/

		model.addAttribute("user", user);

		return "fragments/user-profile-middle";
	}

    @GetMapping("/chat")
    public String chatPage(Model model) {
        User user = userService.getCurrentUser();
        List<User> connectedUsers = userService.getAllUsers()
                .stream()
                .filter(u -> !u.getUsername().equals(user.getUsername()))
                .toList();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("connectedUsers", connectedUsers);
        return "chat";
    }
}
