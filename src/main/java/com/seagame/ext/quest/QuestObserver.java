package com.seagame.ext.quest;

import com.seagame.ext.entities.quest.HeroQuest;
import com.seagame.ext.quest.entities.QuestData;
import com.seagame.ext.quest.entities.QuestProgress;
import com.seagame.ext.quest.entities.TaskProgress;
import io.netty.util.internal.ConcurrentSet;

import java.util.Collection;
import java.util.Set;

/**
 * @author LamHM
 */
public abstract class QuestObserver {
    static final String KEYS_TASK = "tasks";
    static final String KEYI_VALUE = "value";
    static final String KEYI_TIMES = "times";

    public static final int TYPE_KILL = 0;
    public static final int TYPE_COLLECT = 1;
    public static final int TYPE_COLLECT_GIVE = 2;
    public static final int TYPE_GIVE = 3;
    public static final int TYPE_TALK = 5;
    public static final int TYPE_JOIN = 6;
    public static final int TYPE_JOIN_WIN = 7;

    QuestSystem questSystem;

    QuestObserver(QuestSystem questSystem) {
        this.questSystem = questSystem;
        questSystem.addObserver(this);
    }

    public abstract void update(QuestData questData);


    HeroQuest getQuestsByUser(QuestData questData) {
        return questSystem.getOrCreateQuest(questData.getUserID());
//        return questSystem.getOrCreateQuest("1221");
    }

    Set<QuestProgress> getQuestsByHeroQuest(HeroQuest quest, int typeAction) {
        Set<QuestProgress> result = new ConcurrentSet<>();
        quest.getProgressMap().values().stream().filter(QuestProgress::isNotFinish).filter(QuestProgress::isStarted).forEach(questProgress -> {
            if (questProgress.getTasks().stream().anyMatch(taskProgress -> taskProgress.getAction() == typeAction)) {
                result.add(questProgress);
            }
        });
        return result;
    }

    protected abstract int getTaskType();


    /**
     * Xử lý mở quest mới
     */
    private void processNewQuest(HeroQuest heroQuest, QuestProgress completeQuest) {
//        QuestBase quest = QuestConfig.getInstance().getQuest(completeQuest.getIndex());
//        String next = quest.getUnlockQuest();
//        heroQuest.getQuestByIndex(completeQuest.getIndex()).canClaimNow();
//        Arrays.stream(next.split("#")).forEach(s -> heroQuest.buildQuest(QuestConfig.getInstance().getQuest(s)));
    }

    void progressFinishTask(HeroQuest heroQuest, QuestProgress questProgress,
                            TaskProgress taskProgress,
                            Collection<String> questFinishList) {
        questProgress.newTask(taskProgress);
        if (questProgress.getTasks().stream().noneMatch(TaskProgress::isNotFinish)) {
            questFinishList.add(questProgress.getIndex());
            processNewQuest(heroQuest, questProgress);
        }
    }
}
