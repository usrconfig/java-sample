package com.seagame.ext.dao;

import com.seagame.ext.entities.arena.ArenaPower;

import java.util.List;

/**
 * @author LamHM
 *
 */
public interface ArenaSearchRepository {

	int countByPower(long shieldTime, int fromPower, int toPower, int currentTrophy);


	List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int randSkip, int currentTrophy);


	int countByHeroPower(long shieldTime, int fromPower, int toPower, int currentTrophy, int numOfHero);


	List<ArenaPower> findOpponentHero(long shieldTime, int fromPower, int toPower, int randSkip, int currentTrophy,
                                      int numOfHero);

	int countByPower(long shieldTime, int fromPower, int toPower, int arenaPoint, String zone);

	List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int nextInt, int arenaPoint, String zone);
}
