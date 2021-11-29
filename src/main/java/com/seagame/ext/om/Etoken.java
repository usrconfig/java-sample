package com.seagame.ext.om;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;

/**
 * @author LamHM
 */
public class Etoken implements SerializableQAntType {
    private transient String gameHeroId;
    private transient long createTime;
    public long remainSeconds;
    public String code;


    public String getGameHeroId() {
        return gameHeroId;
    }


    public void setGameHeroId(String gameHeroId) {
        this.gameHeroId = gameHeroId;
    }


    public long getCreateTime() {
        return createTime;
    }


    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    public long getRemainSeconds() {
        return remainSeconds;
    }


    public void setRemainSeconds(long remainSeconds) {
        this.remainSeconds = remainSeconds;
    }


    public String getCode() {
        return code;
    }


    public void setCode(String code) {
        this.code = code;
    }

}
