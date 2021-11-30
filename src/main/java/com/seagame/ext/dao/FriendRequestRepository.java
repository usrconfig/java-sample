package com.seagame.ext.dao;

import com.seagame.ext.entities.friend.FriendRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LamHM
 */
@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    @Query(value = "{$or: [{playerId: ?0}, {userRequestId: ?0}]}", delete = true)
    void removeGameData(String gameHeroId);

    @Query(value = "{ userRequestId: ?0, playerId: ?0}", delete = true)
    void removeFriendRequest(String userRequestId, String playerId);


    @Query("{ playerId: ?0}")
    List<FriendRequest> getRequestList(String gameHeroId, Pageable page);

    @Query("{ userRequestId: ?0}")
    List<FriendRequest> getSendRequestList(String gameHeroId, Pageable page);

    @Query(value = "{ playerId: ?0}", count = true)
    int countRequest(String gameHeroId);
}
