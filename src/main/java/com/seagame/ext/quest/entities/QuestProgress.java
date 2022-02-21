package com.seagame.ext.quest.entities;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.config.game.QuestConfig;
import com.seagame.ext.entities.quest.QuestBase;
import com.seagame.ext.quest.QuestSystem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class QuestProgress {
    @Id
    @Field("id")
    private String index;
    @Indexed
    private boolean claim;
    private boolean started;
    private boolean claimed;
    private boolean seen;
    private String group;
    private List<TaskProgress> tasks;
    private long createTime;
    private String deviceToken;

    public QuestProgress() {
    }

    public QuestProgress(QuestBase base) {
        this.index = base.getIndex();
        this.tasks = new ArrayList<>();
        this.started = false;
        initBase(base);
        createTime = System.currentTimeMillis();
        claim = false;
        seen = false;
        this.tasks.forEach(TaskProgress::reset);
    }

    public void initBase(QuestBase base) {
        this.group = base.getGroup();
        TaskProgress taskProgress = new TaskProgress(base.getTaskType(), base.getTask() + "/" + base.getTargetCount());
        taskProgress.setActive(true);
        getTasks().add(taskProgress);
    }


    public void reset() {
        createTime = System.currentTimeMillis();
        claim = false;
        claimed = false;
        seen = false;
        QuestBase quest = QuestConfig.getInstance().getQuest(getIndex());
        started = quest != null && quest.isAutoStart();
        this.tasks.forEach(TaskProgress::reset);
    }

    public boolean isProcessing() {
        return !claim;
    }


    public boolean isCountNotify() {
        return !seen || claim;
    }


    public void canClaimNow() {
        this.claim = true;
    }

    public boolean isNotSeenYet() {
        return !isSeen();
    }

    public QuestProgress setSeen() {
        this.seen = true;
        return this;
    }

    public boolean isFinish() {
        return tasks.size() == 0 || tasks.stream().noneMatch(TaskProgress::isNotFinish);
    }

    public boolean isNotFinish() {
        return !this.isFinish();
    }


    public void newTask(TaskProgress taskProgress) {
//        String unlockTask = taskProgress.getUnlockTask();
//        if (!ItemConfig.isNullOrEmpty(unlockTask)) {
//        }
    }

    public IQAntObject build() {
        IQAntObject antObject = new QAntObject();
        antObject.putUtfString("id", this.index);
        antObject.putBool("claim", this.claim);
        antObject.putBool("claimed", this.claimed);
        antObject.putBool("started", this.started);
        antObject.putUtfString("group", this.group);
        if (tasks.size() > 1) {
            QAntArray array = new QAntArray();
            tasks.forEach(taskProgress -> array.addQAntObject(taskProgress.build()));
            antObject.putQAntArray("tasks", array);
        } else if (tasks.size() == 1) {
            antObject.putQAntObject("task", tasks.get(0).build());
        }
        return antObject;
    }

    public boolean isDailyReset() {
        return group.equals(QuestSystem.DAILY_GROUP) || group.equals(QuestSystem.SPECIAL_GROUP);
    }

    public boolean isDailyQuest() {
        return group.equals("daily");
    }
}
