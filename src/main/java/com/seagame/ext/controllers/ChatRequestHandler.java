package com.seagame.ext.controllers;


import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.chat.ChatConversation;
import com.seagame.ext.entities.chat.ChatMessage;
import com.seagame.ext.services.ChatService;

import java.util.Collection;
import java.util.List;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class ChatRequestHandler extends ZClientRequestHandler {
    private static final int SEND_PRIVATE_CHAT = 1;
    private static final int SEND_PUBLIC_CHAT = 2;
    private static final int SEND_GROUP_CHAT = 3;
    private static final int GET_WORLD_MESSAGE = 4;
    private static final int GET_ROOM_MESSAGE = 5;
    private static final int GET_PRIVATE_MESSAGE = 6;
    private static final int LIST_PRIVATE_MESSAGE = 7;
    private static final int SEEN_PRIVATE_MESSAGE = 8;
    private ChatService chatService;


    public ChatRequestHandler() {
        chatService = ExtApplication.getBean(ChatService.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {

        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }

        switch (action) {
            case GET_WORLD_MESSAGE:
                getMessageList(user, params);
                break;
            case SEND_PRIVATE_CHAT:
                sendPrivateChat(user, params);
                break;
            case SEND_PUBLIC_CHAT:
                sendPublicChat(user, params);
                break;
            case SEND_GROUP_CHAT:
                sendGroupChat(user, params);
                break;
            case GET_ROOM_MESSAGE:
                getRoomMsg(user, params);
                break;
            case GET_PRIVATE_MESSAGE:
                getPrivateMsg(user, params);
                break;
            case LIST_PRIVATE_MESSAGE:
                listPrivateConver(user, params);
                break;
            case SEEN_PRIVATE_MESSAGE:
                seenPrivateMsg(user, params);
                break;
            default:
                break;
        }

    }

    private void seenPrivateMsg(QAntUser user, IQAntObject params) {
        String key = params.getUtfString("id");
        ChatConversation chatConversation = chatService.getChatConversation(key);
        chatConversation.hasSeen();
        params.putQAntObject("conver", chatConversation.build());
        send(params, user);
    }

    private void listPrivateConver(QAntUser user, IQAntObject params) {
        List<ChatConversation> chatConversation = chatService.listPrivateChatConversation(user.getName());
        QAntArray qAntArray = new QAntArray();
        chatConversation.forEach(chatConversation1 -> qAntArray.addQAntObject(chatConversation1.build()));
        params.putQAntArray("list", qAntArray);
        send(params, user);
    }

    private void getPrivateMsg(QAntUser user, IQAntObject params) {
        String received = params.getUtfString("toId");
        ChatConversation chatConversation = chatService.getPrivateChatConversation(user.getName(), received);
        if (chatConversation.getMsg().empty()) {
            ChatMessage privateMsg = chatService.sendPrivateMsg(received, user.getName(), "Joined Chat");
            chatConversation.addMsg(privateMsg);
        }
        params.putQAntObject("conver", chatConversation.build());
        send(params, user);
    }

    private void getRoomMsg(QAntUser user, IQAntObject params) {
        String received = params.getUtfString("roomId");
        ChatConversation chatConversation = chatService.getRoomChatConversation(received);
        params.putQAntObject("conver", chatConversation.build());
        send(params, user);
    }

    private void getMessageList(QAntUser user, IQAntObject params) {
        List<ChatMessage> pageMsgList = chatService.getMsgList();
        QAntArray qAntArray = new QAntArray();
        pageMsgList.forEach(chatMessage -> qAntArray.addQAntObject(chatMessage.build()));
        params.putQAntArray("messages", qAntArray);
        send(params, user);
    }

    private void sendGroupChat(QAntUser user, IQAntObject params) {
    }


    private void sendPublicChat(QAntUser user, IQAntObject params) {
        String msg = params.getUtfString("msg");
        if (msg == null)
            msg = "";
        msg = msg.trim();

        if (msg.length() == 0) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        ChatMessage worldMsg = chatService.sendWorldMsg(user.getName(), msg);
        QAntObject msgObj = worldMsg.build();
        params.putQAntObject("newMsg", msgObj);
        Collection<QAntUser> userList = getParentExtension().getParentZone().getUserList();
        if (userList != null && userList.size() > 0)
            userList.forEach(u -> send(params, u));
    }


    private void sendPrivateChat(QAntUser user, IQAntObject params) {
        String msg = params.getUtfString("msg");
        if (msg == null)
            msg = "";
        msg = msg.trim();

        if (msg.length() == 0) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        String receiverId = params.getUtfString("receiverId");
        ChatMessage privateMsg = chatService.sendPrivateMsg(user.getName(), receiverId, msg);
        QAntObject msgObj = privateMsg.build();
        params.putQAntObject("newMsg", msgObj);
        QAntUser receiverUser = getApi().getUserByName(receiverId);
        if (receiverUser != null) {
            params.putUtfString("converKey", receiverId + "_" + user.getName());
            send(params, receiverUser);
        }
        params.putUtfString("converKey", user.getName() + "_" + receiverId);
        send(params, user);
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_CHAT;
    }
}
