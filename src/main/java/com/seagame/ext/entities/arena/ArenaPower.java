package com.seagame.ext.entities.arena;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.entities.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "arena")
public class ArenaPower implements SerializableQAntType {
    private static final long RESET_TIME_MILI = 14400000;
    private @Id
    String playerId;
    private @Indexed
    String zone;
    public @Indexed
    long arenaPoint;
    public @Indexed
    long totalPoint;
    public @Indexed
    long shieldTime;
    public @Indexed
    int season;
    private int claimedSeason;
    public String name;
    public int rank;
    public int winNo;
    public int refreshNo;
    private transient long beginTime;
    private long resetTime;
    private boolean isBeginner;
    public int accLevel;
    private int searchPower;
    public @Transient
    String avatar;
    private @Transient
    ArenaPower opponent;
    public @Transient
    int arenaPointBuffer;
    @Transient
    private Team atkTeam;
    @Transient
    private Team defTeam;

    private boolean active;


    public ArenaPower() {
        resetTime();
    }


    public IQAntObject buildInfo() {
        IQAntObject arenaInfo = QAntObject.newInstance();
        arenaInfo.putUtfString("name", name);
        arenaInfo.putLong("arenaPoint", arenaPoint);
        arenaInfo.putLong("shieldTime", shieldTime);
        arenaInfo.putUtfString("playerId", playerId);
        arenaInfo.putInt("accLevel", accLevel);
        arenaInfo.putLong("winNo", winNo);
        arenaInfo.putInt("rank", rank);
        arenaInfo.putUtfString("avatar", getAvatar());
        arenaInfo.putInt("power", searchPower);
        if (atkTeam != null)
            arenaInfo.putQAntObject("atkTeam", atkTeam.buildObjectHeroes());
        return arenaInfo;
    }


    public String getAvatar() {
        if (avatar == null)
            avatar = "10";
        return avatar;
    }


    @Override
    public boolean equals(Object obj) {
        ArenaPower arenaInfo = (ArenaPower) obj;
        return arenaInfo.getPlayerId().equals(playerId);
    }

    public void resetTime() {
        resetTime = System.currentTimeMillis() + RESET_TIME_MILI;
    }


    public boolean checkResetTicket() {
        return resetTime - System.currentTimeMillis() <= 0;
    }

    public void incrTrophy(int value) {
        win();
        arenaPoint += value;
        totalPoint += value;
    }


    /**
     * Công thức tính ra âm
     *
     * @param value
     */
    public void decrTrophy(int value) {
        arenaPoint -= value;
        if (arenaPoint <= 0)
            arenaPoint = 0;
        totalPoint -= value;
    }


    public void win() {
        winNo++;
    }


    public void setShieldTime(long millis) {
        this.shieldTime = System.currentTimeMillis() + millis;
    }


    public boolean hasShield() {
        return this.shieldTime - System.currentTimeMillis() > 0;
    }


    public void removeShield() {
        this.shieldTime = 0;
    }

    @Override
    public String toString() {
        return "{playerId: " + playerId + ",heroName:" + name + ", rank:" + rank
                + "}";
    }

    public IQAntObject buildNewbieArenaInfo() {
        return buildInfo();
    }

    public IQAntObject buildInfoWithDef() {
        IQAntObject iqAntObject = buildInfo();
        if (defTeam != null)
            iqAntObject.putQAntObject("defTeam", defTeam.buildObjectHeroes());
        return iqAntObject;
    }
}
