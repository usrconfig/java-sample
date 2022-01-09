package com.seagame.ext.entities.chat;

import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.util.SizedStack;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Stack;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@Document(collection = "chat-conversation")
public class ChatConversation {
    @Id
    private String id;
    private long seen;
    private long last;
    boolean unread;
    private Stack<ChatMessage> msg;

    public Stack<ChatMessage> getMsg() {
        if (msg == null)
            msg = new SizedStack<>(50);
        return msg;
    }

    public void hasSeen() {
        this.seen = this.last;
        unread = false;
    }

    public void addMsg(ChatMessage chatMessage) {
        getMsg().push(chatMessage);
        this.last = chatMessage.getTime();
        unread = this.last > this.seen;
    }

    public QAntObject build() {
        QAntObject qAntObject = new QAntObject();
        qAntObject.putUtfString("id", id);
        qAntObject.putLong("seen", seen);
        qAntObject.putBool("unread", unread);
        QAntArray qAntArray = new QAntArray();
        getMsg().forEach(chatMessage -> qAntArray.addQAntObject(chatMessage.build()));
        qAntObject.putQAntArray("msg", qAntArray);
        return qAntObject;
    }

}
