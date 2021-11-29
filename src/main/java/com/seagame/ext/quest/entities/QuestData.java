package com.seagame.ext.quest.entities;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */

@Getter
@Setter
public class QuestData {
    private IQAntObject data;
    private String userID;
    private int type;

    public QuestData(IQAntObject data, QAntUser user, int type) {
        if (!data.containsKey("value"))
            data.putInt("value", 1);
        this.data = data;
        this.userID = user.getName();
        this.type = type;
    }

    public QuestData(String userID, IQAntObject data, int type) {
        if (!data.containsKey("value"))
            data.putInt("value", 1);
        this.type = type;
        this.data = data;
        this.userID=userID;
    }
}
