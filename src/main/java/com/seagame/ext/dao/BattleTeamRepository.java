package com.seagame.ext.dao;

import com.seagame.ext.entities.team.BattleTeam;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleTeamRepository extends MongoRepository<BattleTeam, String> {

    @Query(value = "{ '_id': ?0}", delete = true)
    void remove(String gameHeroId);
}
