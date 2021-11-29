package com.seagame.ext.entities.arena;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "arena-history")
public class ArenaHistory implements SerializableQAntType {
    public static final int NO_REVENGE = 0;
    public static final int REVENGE = 1;
    public static final int REVENGE_SUCCESS = 2;

    // private static final long EXPIRE_AFTER_SECONDS = 172800;
    private @Id
    String id;
    public @Indexed
    long battleId;
    public @Indexed
    String playerId;
    public boolean isWin;
    // true: no la nguoi tan cong
    public boolean isAttacker;
    // 0:ko duoc tra thu, 1:duoc tra thu, 2:tra thu thanh cong
    public int revengeStatus;
    private String opponent;
    public String opponentInfo;
    public int arenaPoint;
    public long battleTime;
    public boolean seen;
    public boolean replay;
    public String script;
    public boolean npcBattle;
    // exprire sau 2 ngay
    @Indexed(expireAfterSeconds = 172800)
    private Date createAt;


    public ArenaHistory() {
    }


    public ArenaHistory(long battleId, String playerId) {
        this.battleId = battleId;
        this.playerId = playerId;
        this.id = playerId + "#" + battleId;
    }

    public void setBattleTime(long battleTime) {
        this.battleTime = battleTime;
        this.createAt = new Date(battleTime);
    }

    public void setScript(String script) {
        this.script = script;
        replay = StringUtils.isNotBlank(this.script);
    }

}
