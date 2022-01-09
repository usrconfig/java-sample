package com.seagame.ext.entities.chat;

import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.entities.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
public class ChatMessage implements SerializableQAntType {

    public static final int GROUP_WORLD = 1;
    public static final int GROUP_ROOM = 2;
    public static final int GROUP_PRIVATE = 3;

    public static final int TYPE_CHAT = 1;
    public static final int TYPE_SYS = 2;

    public @Id
    long id;
    @Indexed
    public int groupId;
    public String senderId;
    public String senderName;
    public String senderAvatar;
    public String receiveId;
    public String message;
    public int type;
    public long time;

    public ChatMessage() {
    }

    public ChatMessage(String sender, String message) {
        this.id = System.currentTimeMillis();
        this.type = TYPE_CHAT;
        this.senderId = sender;
        this.message = message;
        this.time = System.currentTimeMillis();
    }

    public ChatMessage privateMsg(String receiveId) {
        this.groupId = GROUP_PRIVATE;
        this.receiveId = receiveId;
        return this;
    }

    public ChatMessage roomMsg(String roomId) {
        this.groupId = GROUP_ROOM;
        this.receiveId = roomId;
        return this;
    }

    public ChatMessage worldMsg() {
        this.groupId = GROUP_WORLD;
        return this;
    }

    public ChatMessage buildMoreSenderInfo(Player player) {
        if (player != null) {
            this.senderAvatar = player.getAvatar();
            this.senderName = player.getName();
        }
        return this;
    }

    public QAntObject build() {
        QAntObject qAntObject = new QAntObject();
        qAntObject.putLong("id", id);
        qAntObject.putLong("time", time);
        qAntObject.putInt("group", groupId);
        qAntObject.putInt("type", type);
        qAntObject.putUtfString("senderId", senderId);
        qAntObject.putUtfString("senderName", senderName);
        qAntObject.putUtfString("avatar", senderAvatar);
        if (receiveId != null) {
            qAntObject.putUtfString("receiveId", receiveId);
        }
        if (message != null) {
            qAntObject.putUtfString("message", message);
        }
        return qAntObject;
    }
}
