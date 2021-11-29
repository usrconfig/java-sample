package com.seagame.ext.dao;

import com.seagame.ext.entities.campaign.HeroChapter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @author LamHM
 *
 */
public interface HeroChapterRepository extends MongoRepository<HeroChapter, String> {
	@Query(value = "{ playerId: ?0}", delete = true)
	void remove(String gameHeroId);

	@Query("{ playerId: ?0}")
	List<HeroChapter> findAllChapterByPlayerId(String gameHeroId);

}
