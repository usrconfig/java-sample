package com.seagame.ext.dao;

import com.seagame.ext.entities.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author LamHM
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long> {
    @Query("{groupId:?0, playerId:?1, index:{$gt:?2}}")
    Page<ChatMessage> getMsgList(String groupId, String gameHeroId, long fromIndex, Pageable page);


    @Query("{groupId:?0, index:{$gt:?1}}")
    Page<ChatMessage> getMsgList(String groupId, long fromIndex, Pageable page);


    @Query("{groupId:?0}")
    Page<ChatMessage> getMsgList(String groupId, Pageable page);


    @Query("{groupId:?0,guildId:?1}")
    Page<ChatMessage> getGuildMessageList(String groupId, long guildId, Pageable page);


    @Query("{groupId:?0, $and: [{senderId:?1}, {receiverId:?2}]}")
    Page<ChatMessage> getPrivateMsgList(String groupId, String senderId, String receiverId, Pageable page);


    @Query(value = "{groupId:?0, playerId:?1, index:{$gt:?2}}", count = true)
    int countPrivateMsg(String groupId, String gameHeroId, long fromIndex);


    @Query(value = "{groupId:?0, playerId:?1, index:{$gt:?2}}", count = true)
    int countGuilddMsg(String groupId, long guildId, long fromIndex);


    @Query(value = "{groupId:?0, index:{$gt:?1}}", count = true)
    int countWorldMsg(String groupId, long fromIndex);
}
