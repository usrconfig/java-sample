package com.seagame.ext.dao;

import com.seagame.ext.entities.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LamHM
 */
@Primary
@Repository
public class PlayerRepositoryImpl implements PlayerRepositoryCustom {
    @Autowired
    private MongoOperations mongoOperations;


    @Override
    public List<Player> listGameHero(String server, String nameRegex, int skip, int limit) {
        Query query = new Query(Criteria.where("name").regex(nameRegex, "i").and("zoneName").is(server));
        query.limit(limit);
        query.skip(skip);
        return mongoOperations.find(query, Player.class);
    }

    @Override
    public int countGameHero(String server, String nameRegex) {
        Query query = new Query(Criteria.where("name").regex(nameRegex, "i").and("zoneName").is(server));
        return (int) mongoOperations.count(query, Player.class);
    }
}
