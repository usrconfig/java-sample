package com.seagame.ext.dao;

import com.seagame.ext.entities.quest.HeroQuest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author LamHM
 */
@Repository
public interface QuestRepository extends MongoRepository<HeroQuest, String> {

    void removeAllByPlayerId(String gameHeroId);

    HeroQuest getByPlayerIdAndHeroId(String id, long heroId);
}
