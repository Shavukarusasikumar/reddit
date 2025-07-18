package com.mb.reddit.service.implementation;

import com.mb.reddit.entity.Community;
import com.mb.reddit.entity.Flair;
import com.mb.reddit.repository.CommunityRepository;
import com.mb.reddit.repository.FlairRepository;
import com.mb.reddit.service.FlairService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlairServiceImpl implements FlairService {

    private final FlairRepository flairRepository;
    private final CommunityRepository communityRepository;

    public FlairServiceImpl(FlairRepository flairRepository, CommunityRepository communityRepository) {
        this.flairRepository = flairRepository;
        this.communityRepository = communityRepository;
    }

    @Override
    @Transactional
    public Flair createFlair(long communityId, Flair flair) {
        Community community = communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not Found"));

        flair.setCommunity(community);

        return flairRepository.save(flair);
    }

    @Override
    public void deleteFlairById(long flairId) {
        flairRepository.deleteById(flairId);
    }

    @Override
    public Flair getFlairById(long flairId) {
        return flairRepository.findById(flairId).orElseThrow(() -> new RuntimeException("Flair not found"));
    }

    @Override
    public List<Flair> getAllFlairsByCommunityId(long communityId) {
        return communityRepository.findById(communityId).orElseThrow(() ->
                new RuntimeException("Community Not Found")).getFlairs();
    }
}