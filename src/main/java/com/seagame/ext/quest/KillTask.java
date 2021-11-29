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
public class KillTask extends QuestObserver {
    public KillTask(QuestSystem questSystem) {
        super(questSystem);
    }

    @Override
    public void update(QuestData questData) {
        HeroQuest heroQuest = getQuestsByUser(questData);
        Set<QuestProgress> progresses = getQuestsByHeroQuest(heroQuest, questData.getType());

        QAntTracer.debug(this.getClass(), "[DEBUG] process KillTask");
        if (progresses.size() <= 0)
            return;

        Collection<String> questFinishList = new ArrayList<>();
        Map<String, Integer> countMap = new HashMap<>();
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
        if (questFinishList.size() > 0) {
            questSystem.finishQuest(questFinishList, countMap);
        }
        if (hasChange.get()) {
            questSystem.notifyQuestChange(heroQuest);
            questSystem.save(heroQuest);
        }

    }

    @Override
    protected int getTaskType() {
        return TYPE_KILL;
    }


    public static QuestData init(QAntUser user, String task, int value) {
        IQAntObject data = QAntObject.newInstance();
        data.putUtfString(KEYS_TASK, task);
        data.putInt(KEYI_VALUE, value);
        return new QuestData(data, user, TYPE_KILL);
    }

    public static QuestData init(String gameHeroid, String task, int value) {
        IQAntObject data = QAntObject.newInstance();
        data.putUtfString(KEYS_TASK, task);
        data.putInt(KEYI_VALUE, value);
        return new QuestData(gameHeroid, data, TYPE_KILL);
    }

}
