package com.seagame.ext.quest;

import com.seagame.ext.quest.entities.QuestData;

/**
 * @author LamHM
 */
public class GiveTask extends QuestObserver {
    public GiveTask(QuestSystem questSystem) {
        super(questSystem);
    }

    @Override
    public void update(QuestData questData) {
    }

    @Override
    protected int getTaskType() {
        return TYPE_GIVE;
    }

}