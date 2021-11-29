package com.seagame.ext.dao;

import com.seagame.ext.entities.arena.ArenaHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author LamHM
 *
 */
@Repository
public interface ArenaHistoryRepository extends MongoRepository<ArenaHistory, String> {

	@Query(value = "{playerId :?0}")
	List<ArenaHistory> getHistoryList(String gameHeroId, Pageable page);


	@Query(value = "{playerId :?0}")
	List<ArenaHistory> getHistoryList(String gameHeroId);


	@Query(value = "{playerId:?0, isWin:?1, arenaPoint:{$gt:?2}}")
	List<ArenaHistory> getHistoryList(String gameHeroId, boolean isWin, int geterThanTrophyNo);


	@Query(value = "{playerId:?0, createAt:{$gte:?1}}")
	List<ArenaHistory> getHistoryList(String gameHeroId, Date fromDate);


	@Query(value = "{playerId:?0, battleTime:{$gte:?1}}")
	List<ArenaHistory> getHistoryList(String gameHeroId, long fromMillis);


	@Query(value = "{ playerId: ?0, $or: [{seen: {$exists: false}}, {seen: false}]}", count = true)
	int countNewHistory(String playerId);


	@Query(value = "{'id' : {'$in' : ?0}}", delete = true)
	void removeData(List<Long> ids);


	@Query("{'id' :?0}")
	List<ArenaHistory> getBattle(long id);

}
