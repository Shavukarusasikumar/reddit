package com.mb.reddit.controller;

import com.mb.reddit.entity.JoinRequest;
import com.mb.reddit.service.JoinRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @GetMapping("/join-requests/{communityId}")
    public String getJoinCommunityRequest(@PathVariable Long communityId, Model model){
        List<JoinRequest> joinRequests = joinRequestService.getJoinCommunityRequest(communityId);

        model.addAttribute("joinRequests", joinRequests);

        return "join-requests";
    }

    @PostMapping("/{id}/accept")
    public String acceptRequest(@PathVariable Long id) {
        Long communityId =  joinRequestService.acceptJoinRequest(id);

        return "redirect:/join-requests/" + communityId;
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Long id) {
        Long communityId = joinRequestService.rejectJoinRequest(id);

        return "redirect:/join-requests/" + communityId;
    }
}
