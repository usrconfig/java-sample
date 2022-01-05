package com.seagame.ext.entities.campaign;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "daily-event")
public class HeroDailyEvent implements SerializableQAntType {
    private @Id
    String id;
    private String playerId;
    private long heroId;
    public String eventGroup;
    public String stageIdx;
    public int chance;
    private int maxChance;


    public HeroDailyEvent() {
    }


    public HeroDailyEvent(String playerId, long heroId, DailyEvent dailyEvent) {
        this.id = genKey(playerId + heroId, dailyEvent.getStage());
        this.playerId = playerId;
        this.heroId = heroId;
        this.eventGroup = dailyEvent.getGroup();
        this.stageIdx = dailyEvent.getStage();
        this.chance = 1;
        this.maxChance = 1;
    }


    public static String genKey(String gameHeroId, String groupIndex) {
        return gameHeroId + "_" + groupIndex;
    }


    public void decrChance() {
        chance--;
    }

    public void resetChance() {
        this.chance = maxChance;
    }

    public boolean isMaxChance() {
        return this.chance >= maxChance;
    }

    public void incrChance(int maxChance) {
        int delta = maxChance - this.maxChance;
        this.maxChance = maxChance;
        if (chance + delta <= maxChance)
            chance += delta;
        else
            chance = maxChance;

    }

    public void vipResetChance(int bonus) {
        maxChance = bonus;
        chance = bonus;
    }

    public IQAntObject build() {
        IQAntObject iqAntObject = new QAntObject();
        iqAntObject.putUtfString("stageIdx", stageIdx);
        iqAntObject.putUtfString("eventGroup", eventGroup);
        iqAntObject.putInt("chance", chance);
        iqAntObject.putInt("maxChance", maxChance);
        return iqAntObject;
    }
}
