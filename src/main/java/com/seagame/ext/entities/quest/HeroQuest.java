package com.seagame.ext.entities.quest;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.config.game.QuestConfig;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.quest.entities.QuestProgress;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
@ToString
@Document(collection = "quests")
public class HeroQuest implements SerializableQAntType {
    public String id;
    public String playerId;
    public long heroId;
    private Map<String, QuestProgress> progressMap;

    public HeroQuest() {
    }

    public HeroQuest(String id, long heroId) {
        this.id = id + "#" + heroId;
        this.playerId = id;
        this.heroId = heroId;
        progressMap = new ConcurrentHashMap<>();
    }

    public String getId() {
        return playerId;
    }

    public Map<String, QuestProgress> getProgressMap() {
        return progressMap;
    }


    public QuestProgress getQuestByIndex(String index) {
        return progressMap.get(index);
    }

    public void resetDaily() {
        progressMap.values().stream().filter(QuestProgress::isDailyReset).forEach(QuestProgress::reset);
    }

    public void buildQuestList(List<QuestBase> quests, int level) {
        quests.forEach(questBase -> {
            QuestProgress questProgress = new QuestProgress(questBase);
            if (questBase.isAutoStart()) {
                questProgress.setStarted(true);
            }
            progressMap.put(questProgress.getIndex(), questProgress);
        });
        buildLimitQuestList(quests, level, QuestSystem.CLAN_GROUP, 6);
    }

    public void buildLimitQuestList(List<QuestBase> quests, int level, String group, int total) {
        List<QuestProgress> collect = progressMap.values().stream().filter(questProgress -> questProgress.getGroup().equals(group)).collect(Collectors.toList());
        collect.forEach(aBoolean -> progressMap.remove(aBoolean.getIndex()));
        List<QuestBase> limit = quests.stream().filter(questBase -> questBase.getGroup().equals(group)).limit(total).collect(Collectors.toList());
        Collections.shuffle(limit);
        limit.stream().limit(total).forEach(questBase -> {
            QuestProgress questProgress = new QuestProgress(questBase);
            if (questBase.isAutoStart()) {
                questProgress.setStarted(true);
            }
            progressMap.put(questProgress.getIndex(), questProgress);
        });
    }

    public void buildQuest(QuestBase questBase) {
        progressMap.putIfAbsent(questBase.getIndex(), new QuestProgress(questBase));
    }


    public void initBase() {
        progressMap.values().forEach(questProgress -> questProgress.initBase(QuestConfig.getInstance().getQuest(questProgress.getIndex())));
    }

    public IQAntObject buildHeroQuest() {
        IQAntObject antObject = new QAntObject();
        antObject.putUtfString("id", this.id);
        antObject.putUtfString("playerId", this.playerId);
        antObject.putLong("heroId", this.heroId);
        QAntArray array = new QAntArray();
//        progressMap.values().stream().filter(QuestProgress::isStarted).forEach(questProgress -> array.addQAntObject(questProgress.build()));
        progressMap.values().stream().forEach(questProgress -> array.addQAntObject(questProgress.build()));
        antObject.putQAntArray("progressMap", array);
        return antObject;
    }


    public void resetClanDaily(List<QuestBase> quests, int level) {
        buildLimitQuestList(quests, level, QuestSystem.CLAN_GROUP, 6);
    }

//    public Collection<QuestProgress> getClaimAbleByGroup(String group) {
//        return progressMap.values().stream().filter(questProgress -> questProgress.getGroupId().equals(group)).filter(QuestProgress::isClaim).filter(questProgress -> !questProgress.isClaimed()).collect(Collectors.toList());
//    }
}
