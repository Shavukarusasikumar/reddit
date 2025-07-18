package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Topic;
import com.mb.reddit.repository.TopicRepository;
import com.mb.reddit.service.TopicService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;

    public TopicServiceImpl(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }
}
