package com.seagame.ext.dao;

import com.seagame.ext.entities.friend.BuddyList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author LamHM
 */
@Repository
public interface BuddyListRepository extends MongoRepository<BuddyList, String> {
    @Query(value = "{playerId:?0}", fields = "{newFriendNo:1}")
    BuddyList getBuddyListContainFields(String gameHeroId);
}
