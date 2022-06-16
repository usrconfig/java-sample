package com.seagame.ext.quest;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.entities.quest.HeroQuest;
import com.seagame.ext.quest.entities.QuestData;
import com.seagame.ext.quest.entities.QuestProgress;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author LamHM
 */
public class CollectionTask extends QuestObserver {
    public CollectionTask(QuestSystem questSystem) {
        super(questSystem);
    }

    @Override
    public void update(QuestData questData) {
        HeroQuest heroQuest = getQuestsByUser(questData);
        Set<QuestProgress> progresses = getQuestsByHeroQuest(heroQuest, questData.getType());

        QAntTracer.warn(this.getClass(), "[DEBUG] process CollectTask");
        if (progresses.size() <= 0)
            return;

        Collection<QuestProgress> questFinishList = new ArrayList<>();
        IQAntObject data = questData.getData();
        String task = data.getUtfString(KEYS_TASK);
        int value = data.getInt(KEYI_VALUE);
        AtomicBoolean hasChange = new AtomicBoolean(false);
        progresses.forEach(questProgress -> questProgress.getTasks().forEach(taskProgress -> {
                    if (taskProgress.isApplyAble() && task.equals(taskProgress.getTaskKey())) {
                        if (!hasChange.get())
                            hasChange.set(true);
                        if (taskProgress.incr(value)) {
                            this.progressFinishTask(heroQuest, questProgress, taskProgress, questFinishList);
                        }
                    }
                }
        ));
        if (hasChange.get()) {
            hasChange.set(false);
            questSystem.notifyQuestChange(heroQuest);
            questSystem.save(heroQuest);
        }
        if (questFinishList.size() > 0) {
            questSystem.finishQuest(heroQuest.getPlayerId(), questFinishList);
        }
    }

    @Override
    protected int getTaskType() {
        return TYPE_COLLECT;
    }


    public static QuestData init(QAntUser user, String task, int value) {
        IQAntObject data = QAntObject.newInstance();
        data.putUtfString(KEYS_TASK, task);
        data.putInt(KEYI_VALUE, value);
        return new QuestData(data, user, TYPE_COLLECT);
    }

    public static QuestData init(String gameHeroid, String task, int value) {
        IQAntObject data = QAntObject.newInstance();
        data.putUtfString(KEYS_TASK, task);
        data.putInt(KEYI_VALUE, value);
        return new QuestData(gameHeroid, data, TYPE_COLLECT);
    }

}
