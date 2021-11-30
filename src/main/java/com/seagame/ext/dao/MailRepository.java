package com.seagame.ext.dao;

import com.seagame.ext.entities.mail.Mail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author LamHM
 */
@Repository
public interface MailRepository extends MongoRepository<Mail, Long> {
    @Query("{'playerId' : ?0}")
    List<Mail> getInboxMailList(String gameHeroId, Pageable page);

    List<Mail> getAllByPlayerId(String gameHeroId);

    @Query("{playerId : {$in : ?0}, type : ?1}")
    List<Mail> getInboxMailList(List<String> gameHeroIds, int type);


    @Query("{title:?0}")
    List<Mail> getInboxMailList(String title);


    @Query(value = "{'playerId' : ?0, $or: [{seen: false}, {claim:true}]}", count = true)
    int countNtf(String gameHeroId);

    int countInboxMailByPlayerId(String gameHeroId);


    @Query("{'playerId' : ?0, 'claim':true}")
    List<Mail> getMailClaimIsTrue(String gameHeroId);


    @Query("{'id':?0, 'playerId' : ?1}")
    Mail getMail(long id, String gameHeroId);


    @Query(value = "{ playerId: ?0}", delete = true)
    void remove(String gameHeroId);


    @Query(value = "{'id' : {'$in' : ?0}}", delete = true)
    void remove(Collection<Long> ids);

    @Query("{'sender' : ?0}")
    List<Mail> getOutboxMailList(String gameHeroId, Pageable page);

    int countOutboxMailBySender(String gameHeroId);
}
