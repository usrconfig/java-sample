package com.seagame.ext.dao;

import com.seagame.ext.entities.campaign.HeroStage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author LamHM
 *
 */
@Repository
public interface HeroStageRepository extends MongoRepository<HeroStage, Long> {

	@Query("{'playerId' : ?0, 'chapterIndex' : ?1}")
	List<HeroStage> findStages(String playerId, int chapterId);


	@Query("{'playerId' : ?0, 'mode' : 'easy', 'clear' : false}")
	HeroStage findCurrentStage(String playerId);


	@Query("{'playerId' : ?0, 'index' : ?1}")
	HeroStage findStage(String playerId, int index);


	@Query("{playerId : ?0, 'index' : {$in : ?1}}")
	List<HeroStage> findStagesIn(String playerId, Set<Integer> idxs);


	@Query(value = "{ playerId: ?0}", delete = true)
	void remove(String gameHeroId);


	@Query("{ playerId: ?0}")
	List<HeroStage> findAllStageByPlayerId(String gameHeroId);


	@Query(value = "{ playerId: ?0, index:?1}", count = true)
	int countDuplicateChapter(String playerId, int index);


	@Query("{playerId : ?0, chapterIndex : ?1, mode: ?2, clear:false}")
	HeroStage findStageByChapterIndexAndMode(String playerId, int chapterIndex, String mode);
}
