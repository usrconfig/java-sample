package com.seagame.ext.services;

import com.seagame.ext.dao.ChatConversationRepository;
import com.seagame.ext.entities.chat.ChatConversation;
import com.seagame.ext.entities.chat.ChatMessage;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.util.SizedStack;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Stack;

/**
 * @author LamHM
 */
@Service
public class ChatService implements InitializingBean {
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    private HeroClassManager heroClassManager;

    private Stack<ChatMessage> worldChat;

    public Stack<ChatMessage> getWorldChat() {
        if (worldChat == null)
            worldChat = new SizedStack<>(50);
        return worldChat;
    }

    public List<ChatMessage> getMsgList() {
        return getWorldChat();
    }

    public ChatMessage sendWorldMsg(String name, String msg) {
        ChatMessage message = new ChatMessage(name, msg).worldMsg().buildMoreSenderInfo(playerManager.getPlayer(name));
        getWorldChat().push(message);
        return message;
    }

    public ChatConversation getChatConversation(String chatKey) {
        if (chatConversationRepository.existsById(chatKey)) {
            return chatConversationRepository.findById(chatKey).get();
        } else {
            ChatConversation chatConversation = new ChatConversation();
            chatConversation.setId(chatKey);
            return chatConversationRepository.save(chatConversation);
        }
    }

    public ChatConversation getRoomChatConversation(String roomId) {
        return getChatConversation(roomId);
    }

    public ChatConversation getPrivateChatConversation(String name, String receiverId) {
        return getChatConversation(name + "_" + receiverId);
    }

    public List<ChatConversation> listPrivateChatConversation(String name) {
        return chatConversationRepository.getPrivateMsgList(name, PageRequest.of(0, 20));
    }

    public ChatMessage sendPrivateMsg(String name, String receiverId, String msg) {
        ChatMessage chatMessage = new ChatMessage(name, msg).privateMsg(receiverId).buildMoreSenderInfo(playerManager.getPlayer(name));
        pushMsg(name + "_" + receiverId,chatMessage);
        pushMsg(receiverId + "_" + name,chatMessage);
        return chatMessage;
    }
    public void pushMsg(String id,ChatMessage chatMessage){
        ChatConversation chatConversation = getChatConversation(id);
        chatConversation.addMsg(chatMessage);
        chatConversationRepository.save(chatConversation);
    }

    public ChatMessage sendRoomMsg(String name, String receiverId, String msg) {
        ChatMessage chatMessage = new ChatMessage(name, msg).roomMsg(receiverId).buildMoreSenderInfo(playerManager.getPlayer(name));
        pushMsg(receiverId,chatMessage);
        return chatMessage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        chatConversationRepository.findOne("taolao");
//        ChatMessage chatMessage = new ChatMessage("taolaonet", "test").privateMsg("toId");
//        ChatConversation chatConversation=new ChatConversation();
//        chatConversation.setId("taolao");
//        chatConversation.buildTest(chatMessage);
//        chatConversationRepository.save(chatConversation);
    }
}
