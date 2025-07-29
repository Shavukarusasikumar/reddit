package com.mb.reddit.controller;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Topic;
import com.mb.reddit.service.CommunityService;
import com.mb.reddit.service.TopicService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class TopicController {

	private final TopicService topicService;
	private final CommunityService communityService;

	public TopicController(TopicService topicService, CommunityService communityService) {
		this.topicService = topicService;
		this.communityService = communityService;
	}

	@GetMapping("/topics")
	public String topicsPage(@RequestParam(required = false) Long topicId, Model model) {
		List<Topic> topics = topicService.getAllTopics();
		model.addAttribute("topics", topics);

		if (topicId != null) {
			Topic selectedTopic = topicService.getTopicById(topicId);

			if (selectedTopic != null) {
				List<Community> communities = communityService.getCommunitiesByTopicId(topicId);

				model.addAttribute("selectedTopic", selectedTopic);
				model.addAttribute("communities", communities);
			}
		}

		return "topics";
	}
}