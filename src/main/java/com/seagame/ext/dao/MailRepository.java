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
    @Query("{'heroId' : ?0}")
    List<Mail> getMailList(long heroId, Pageable page);

    List<Mail> getAllByHeroId(long heroId);

    @Query("{heroId : {$in : ?0}, type : ?1}")
    List<Mail> getMailList(List<Long> heroIds, int type);


    @Query("{title:?0}")
    List<Mail> getMailList(String title);


    @Query(value = "{'heroId' : ?0, $or: [{seen: false}, {claim:true}]}", count = true)
    int countNtf(long heroId);

    int countMailByHeroId(long heroId);


    @Query("{'heroId' : ?0, 'claim':true}")
    List<Mail> getMailClaimIsTrue(long heroId);


    @Query("{'id':?0}")
    Mail getMail(long id);


    @Query(value = "{ heroId: ?0}", delete = true)
    void remove(long heroId);


    @Query(value = "{'id' : {'$in' : ?0}}", delete = true)
    void remove(Collection<Long> ids);
}
