/**
 * 
 */
package com.seagame.ext.dao.impl;

import com.seagame.ext.dao.ArenaSearchRepository;
import com.seagame.ext.entities.arena.ArenaPower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author NhaTran
 *
 */
@Primary
@Repository
public class ArenaSearchHeroPowerRepositoryImpl implements ArenaSearchRepository {
	@Autowired
	private MongoOperations mongoOperations;

	private final int MaxTrophy = 60000;
	private final int MinTrophy = 20000;


	@Override
	public List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int skip, int currentTrophy) {
		// TODO random list result
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;
		// Query query = new Query(
		// Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower").gte(fromPower).lte(toPower));
		Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower").gte(fromPower)
				.lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy));
		query.limit(3);
		query.skip(skip);
		return mongoOperations.find(query, ArenaPower.class);
	}


	@Override
	public int countByPower(long shieldTime, int fromPower, int toPower, int currentTrophy) {
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;
		return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower")
				.gte(fromPower).lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy)), ArenaPower.class);

	}


	@Override
	public int countByHeroPower(long shieldTime, int fromPower, int toPower, int currentTrophy, int numOfHero) {
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;
		return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime)
				.and("team.searchPower").gte(fromPower).lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy)),
				ArenaPower.class);
	}


	@Override
	public List<ArenaPower> findOpponentHero(long shieldTime, int fromPower, int toPower, int randSkip,
			int currentTrophy, int numOfHero) {
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;

		Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("team.searchPower").gte(fromPower)
				.lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy));
		query.limit(3);
		query.skip(randSkip);

		return mongoOperations.find(query, ArenaPower.class);
	}

	@Override
	public int countByPower(long shieldTime, int fromPower, int toPower, int currentTrophy, String zone) {
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;
		return (int) mongoOperations.count(new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower")
				.gte(fromPower).lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy).and("_id").regex("^"+zone)), ArenaPower.class);

	}

	@Override
	public List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int skip, int currentTrophy, String zone) {
		// TODO random list result
		int minTrophy = Math.max(currentTrophy - MinTrophy, 0);
		int maxTrophy = currentTrophy + MaxTrophy;
		// Query query = new Query(
		// Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower").gte(fromPower).lte(toPower));
		Query query = new Query(Criteria.where("shieldTime").lte(shieldTime).and("searchMaxPower").gte(fromPower)
				.lte(toPower).and("arenaPoint").gte(minTrophy).lte(maxTrophy).and("_id").regex("^"+zone));
		query.limit(3);
		query.skip(skip);
		return mongoOperations.find(query, ArenaPower.class);
	}
}
