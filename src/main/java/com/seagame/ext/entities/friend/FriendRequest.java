package com.seagame.ext.entities.friend;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "friend-request")
public class FriendRequest implements SerializableQAntType {
    @Id
    private String id;
    @Indexed(expireAfterSeconds = 172800)
    private long requestTime;
    private @Indexed
    String playerId; //người nhận
    public String userRequestId;//người gửi
    public boolean seen;


    public FriendRequest() {
    }


    public FriendRequest(String userRequestId, String userReceiveId) {
        this.id = genRequestId(userRequestId, userReceiveId);
        this.requestTime = System.currentTimeMillis();
        this.userRequestId = userRequestId;
        this.playerId = userReceiveId;
    }


    public static String genRequestId(String userRequestId, String userReceiveId) {
        return userRequestId + "_" + userReceiveId;
    }


    public IQAntObject buildObject() {
        QAntObject object = QAntObject.newInstance();
        object.putUtfString("id", id);
        object.putLong("requestTime", requestTime);
        object.putUtfString("userRequestId", userRequestId);
        object.putBool("seen", seen);
        return object;
    }

}
