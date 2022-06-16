package com.seagame.ext.dao;

import com.seagame.ext.entities.arena.ArenaPower;

import java.util.List;

/**
 * @author LamHM
 */
public interface ArenaSearchRepository {
    int countByPower(long shieldTime, int fromPower, int toPower, int arenaPoint, String zone);

    List<ArenaPower> findOpponent(long shieldTime, int fromPower, int toPower, int nextInt, int arenaPoint, String zone);

    int countByPower(int fromPower, int toPower, int arenaPoint, String zone);

    List<ArenaPower> findOpponent(int fromPower, int toPower, int nextInt, int arenaPoint, String zone);
}
