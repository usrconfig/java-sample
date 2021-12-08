package com.seagame.ext.dao.impl;

import com.seagame.ext.dao.ArenaSearchRepository;
import com.seagame.ext.entities.arena.ArenaPower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LamHM
 */

@Repository
public class ArenaSearchRepositoryImpl implements ArenaSearchRepository {
    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public int countByPower(int fromPower, int toPower, int arenaPoint, String zone) {
        return (int) mongoOperations.count(new Query(Criteria.where("searchPower")
                .gte(fromPower).lte(toPower).and("arenaPoint").gte(0).and("zone").is(zone)), ArenaPower.class);
    }

    @Override
    public List<ArenaPower> findOpponent(int fromPower, int toPower, int skip, int arenaPoint, String zone) {
        Query query = new Query(Criteria.where("searchPower").gte(fromPower)
                .lte(toPower).and("arenaPoint").gte(0).and("zone").is(zone));
        query.limit(4);
        query.skip(skip);

        return mongoOperations.find(query, ArenaPower.class);
    }

//    @Override
//    public int countByPower(long shieldTime, int fromPower, int toPower, int arenaPoint, String zone) {
//        return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchPower")
//                .gte(fromPower).lte(toPower).and("arenaPoint").gte(0).and("zone").is(zone)), ArenaPower.class);
//    }
//
//    @Override
//    public List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int skip, int arenaPoint, String zone) {
//        Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchPower").gte(fromPower)
//                .lte(toPower).and("arenaPoint").gte(0).and("zone").is(zone));
//        query.limit(4);
//        query.skip(skip);
//
//        return mongoOperations.find(query, ArenaPower.class);
//    }

}
