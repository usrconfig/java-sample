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
 * 
 * @author LamHM
 *
 */

@Repository
public class ArenaSearchRepositoryImpl implements ArenaSearchRepository {
	@Autowired
	private MongoOperations mongoOperations;


	@Override
	public List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int skip, int currentTrophy) {
		Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("team.teamPower").gte(fromPower)
				.lte(toPower).and("arenaPoint").gt(500));
		query.limit(3);
		query.skip(skip);

		return mongoOperations.find(query, ArenaPower.class);
	}


	@Override
	public int countByPower(long shieldTime, int fromPower, int toPower, int currentTrophy) {
		return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime).and("team.teamPower")
				.gte(fromPower).lte(toPower).and("arenaPoint").gt(500)), ArenaPower.class);
	}


	@Override
	public int countByHeroPower(long shieldTime, int fromPower, int toPower, int currentTrophy, int numOfHero) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public List<ArenaPower> findOpponentHero(long shieldTime, int fromPower, int toPower, int randSkip,
			int currentTrophy, int numOfHero) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countByPower(long shieldTime, int fromPower, int toPower, int arenaPoint, String zone) {
		return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime).and("team.teamPower")
				.gte(fromPower).lte(toPower).and("arenaPoint").gt(500).and("_id").regex("^"+zone)), ArenaPower.class);
	}

	@Override
	public List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int skip, int arenaPoint, String zone) {
		Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("team.teamPower").gte(fromPower)
				.lte(toPower).and("arenaPoint").gt(500).and("_id").regex("^"+zone));
		query.limit(3);
		query.skip(skip);

		return mongoOperations.find(query, ArenaPower.class);
	}

}
