package com.mb.reddit.service;

import com.mb.reddit.entity.Flair;

import java.util.List;

public interface FlairService {

    Flair createFlair(long communityId, Flair flair);

    void deleteFlairById(long flairId);

    Flair getFlairById(long flairId);

    List<Flair> getAllFlairsByCommunityId(long communityId) ;

}