package com.seagame.ext.dao;

import com.seagame.ext.entities.chat.ChatConversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LamHM
 */
@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversation, String> {
    @Query("{ 'id' : { '$regex' : ?0 , $options: 'i'}, 'notSeen' : true }")
    List<ChatConversation> getPrivateMsgList(String senderId, Pageable page);

}
