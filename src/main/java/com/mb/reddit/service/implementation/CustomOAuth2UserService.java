package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.CustomUserDetails;
import com.mb.reddit.entity.User;
import com.mb.reddit.repository.UserRepository;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	public CustomOAuth2UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);

		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		String picture = oauth2User.getAttribute("picture");

		if (email == null) {
			throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
		}

		Optional<User> existingUser = userRepository.findUserByEmail(email);
		User user;

		if (existingUser.isPresent()) {
			user = existingUser.get();
		} else {
			User newUser = new User();
			newUser.setEmail(email);
			newUser.setUsername(generateUniqueUsername(name, email));
			newUser.setProfilePicture(picture);
			newUser.setBio("");
			newUser.setPassword("$2a$12$/ayDca8LhV7cNuCKs7BTl.q.l9STeA.HlGfS.YofUZgHYU4o2cFgS");
			user = userRepository.save(newUser);
		}

		return new CustomUserDetails(user, oauth2User.getAttributes());
	}

	private String generateUniqueUsername(String name, String email) {
		String baseUsername = name != null ? name.replaceAll("\\s+", "").toLowerCase()
				: email.split("@")[0].toLowerCase();

		String username = baseUsername;
		int counter = 1;

		while (userRepository.existsByUsername(username)) {
			username = baseUsername + counter;
			counter++;
		}

		return username;
	}
}