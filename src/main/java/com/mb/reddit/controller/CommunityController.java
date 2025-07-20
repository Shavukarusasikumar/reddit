package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.User;
import com.mb.reddit.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CommunityController {

	private final CommunityService communityService;

	public  CommunityController(CommunityService communityService) {
		this.communityService = communityService;
	}

	@PostMapping
	public ResponseEntity<Community> createCommunity(@RequestBody Community community,
													 @AuthenticationPrincipal User creator) {
		community.setCreator(creator);
		Community createdCommunity = communityService.createCommunity(community);
		return ResponseEntity.ok(createdCommunity);
	}

	@DeleteMapping("/{communityId}")
	public ResponseEntity<Void> deleteCommunity(@PathVariable Long communityId,
												@AuthenticationPrincipal User user) {
		User creator = communityService.getCreatorByCommunityId(communityId);
		if (!creator.getId().equals(user.getId())) {
			return ResponseEntity.status(403).build();
		}
		communityService.deleteCommunity(communityId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{communityId}/members")
	public ResponseEntity<List<User>> getCommunityMembers(@PathVariable Long communityId) {
		List<User> members = communityService.getCommunityMembers(communityId);
		return ResponseEntity.ok(members);
	}

	@GetMapping("/{communityId}/members/count")
	public ResponseEntity<Long> getMembersCount(@PathVariable Long communityId) {
		Long count = communityService.getMembersCountByCommunityId(communityId);
		return ResponseEntity.ok(count);
	}

	@PostMapping("/{communityId}/join")
	public ResponseEntity<Void> joinCommunity(@PathVariable Long communityId,
											  @AuthenticationPrincipal User user) {
		communityService.addMemberByCommunityId(user, communityId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{communityId}/creator")
	public ResponseEntity<User> getCommunityCreator(@PathVariable Long communityId) {
		User creator = communityService.getCreatorByCommunityId(communityId);
		return ResponseEntity.ok(creator);
	}

	@GetMapping("/{communityId}/view")
	public String getCommunityView(@PathVariable Long communityId, Model model,
								   @AuthenticationPrincipal User currentUser) {
		Community community = communityService.getCommunityById(communityId);
		model.addAttribute("community", community);

		// Check if current user is creator
		boolean isCreator = currentUser != null &&
				community.getCreator().getId().equals(currentUser.getId());
		model.addAttribute("isCreator", isCreator);

		// Check if user has joined
		boolean hasJoined = currentUser != null &&
				community.getMembers().contains(currentUser);
		model.addAttribute("hasJoined", hasJoined);

		return "community-profile"; // This should match your Thymeleaf template name
	}

	@GetMapping
	public ResponseEntity<List<Community>> getAllCommunities() {
		List<Community> communities = communityService.getAllCommunities();
		return ResponseEntity.ok(communities);
	}

	@GetMapping("/joined")
	public ResponseEntity<List<Community>> getJoinedCommunities(@AuthenticationPrincipal User user) {
		List<Community> communities = communityService.getAllCommunities(); // You might want to create a specific service method for this
		// Filter communities where user is a member
		communities.removeIf(community -> !community.getMembers().contains(user));
		return ResponseEntity.ok(communities);
	}

	@GetMapping("/created")
	public ResponseEntity<List<Community>> getCreatedCommunities(@AuthenticationPrincipal User user) {
		List<Community> communities = communityService.getAllCommunities(); // You might want to create a specific service method for this
		// Filter communities where user is the creator
		communities.removeIf(community -> !community.getCreator().getId().equals(user.getId()));
		return ResponseEntity.ok(communities);
	}
}