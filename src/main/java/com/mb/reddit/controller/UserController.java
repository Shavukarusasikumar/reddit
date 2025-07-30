package com.mb.reddit.controller;

import com.mb.reddit.dto.UserKarmaDTO;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.User;

import com.mb.reddit.repository.UserRepository;

import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.NotificationService;
import com.mb.reddit.service.UserService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;
    private final NotificationService notificationService;
    private final CommunityService communityService;

    public UserController(UserRepository userRepository,
                          UserService userService, NotificationService notificationService,
                          CommunityService communityService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.communityService = communityService;
    }

    @GetMapping("/login")
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

        return "redirect:/user/login";
    }

    @GetMapping("/user")
    public String getUserProfilePage(Model model) {
        User user = userService.getCurrentUser();

        if (user == null) {
            return "redirect:/user/login";
        }

        UserKarmaDTO karmaDTO = userService.getKarmaDto(user.getId());

        Integer notificationCount = notificationService.getNotificationCount();

        List<Community> joinedCommunities = communityService.findUserJoinedCommunities();
        List<Community> recentCommunities = joinedCommunities.stream().limit(5).toList();

        model.addAttribute("showAllTabs", true);
        model.addAttribute("communities", joinedCommunities);
        model.addAttribute("recentCommunities", recentCommunities);
        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("karma", karmaDTO);
        model.addAttribute("user", user);

        return "fragments/user-profile-middle";
    }

    @GetMapping("/chat")
    public String chatPage(Model model) {
        User user = userService.getCurrentUser();

        int notificationCount = notificationService.getNotificationCount();

        List<User> connectedUsers = userService.getAllUsers()
                .stream()
                .filter(u -> !u.getUsername().equals(user.getUsername()))
                .toList();

        model.addAttribute("notificationCount", notificationCount);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("connectedUsers", connectedUsers);

        return "chat";
    }

   @GetMapping("/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {
       Boolean showAllTab = false;

       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

       if (authentication != null && authentication.isAuthenticated() &&
               !(authentication.getPrincipal() instanceof String)) {
           CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

           if(userDetails.getId().equals(userId)){
               showAllTab = true;
           }
       }

       User user = userService.getUserById(userId);

       UserKarmaDTO karmaDTO = userService.getKarmaDto(user.getId());

       Integer notificationCount = notificationService.getNotificationCount();

       List<Community> joinedCommunities = communityService.findUserJoinedCommunities();
       List<Community> recentCommunities = joinedCommunities.stream().limit(5).toList();

       model.addAttribute("showAllTabs", showAllTab);
       model.addAttribute("communities", joinedCommunities);
       model.addAttribute("recentCommunities", recentCommunities);
       model.addAttribute("notificationCount", notificationCount);
       model.addAttribute("karma", karmaDTO);
       model.addAttribute("user", user);

       return "fragments/user-profile-middle";
    }
}
