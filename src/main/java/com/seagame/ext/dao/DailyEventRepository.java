package com.seagame.ext.dao;

import com.seagame.ext.entities.campaign.HeroDailyEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyEventRepository extends MongoRepository<HeroDailyEvent, String> {

    @Query("{'playerId' : ?0}")
    List<HeroDailyEvent> getAllEvent(String playerId);

    @Query("{'playerId' : ?0, 'stageIdx' : ?1}")
    HeroDailyEvent getEvent(String playerId, String stageIdx);


    @Query(value = "{ playerId: ?0}", delete = true)
    void remove(String gameHeroId);
}
