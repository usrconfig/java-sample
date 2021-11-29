package com.seagame.ext.dao;

import com.seagame.ext.entities.arena.ArenaPower;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author LamHM
 */
@Repository
public interface ArenaPowerRepository extends MongoRepository<ArenaPower, String> {

    @Query(value = "{season:?0, arenaPoint: { $gt: ?1 }, zone:?2}", count = true)
    int getRank(int season, long cupNo, String zone);


    @Query(value = "{ _id: ?0}", delete = true)
    void remove(String gameHeroId);

    @Query(value = "{season:?0, zone:?1}")
    List<ArenaPower> getTopArenaPoint(int season, String zone, PageRequest page);
}
