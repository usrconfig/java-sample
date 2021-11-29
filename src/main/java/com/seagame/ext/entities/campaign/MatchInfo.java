package com.seagame.ext.entities.campaign;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */
@Getter
@Setter
public class MatchInfo {

    private String event;
    private String group;
    private String playerId;

    public MatchInfo(String playerId, String event, String group) {
        this.event = event;
        this.group = group;
        this.playerId = playerId;
    }

    public IQAntObject buildMatchInfo() {
        QAntObject qAntObject = new QAntObject();
        qAntObject.putUtfString("event", event);
        qAntObject.putUtfString("group", group);
        qAntObject.putUtfString("playerId", playerId);
        return qAntObject;
    }
}
