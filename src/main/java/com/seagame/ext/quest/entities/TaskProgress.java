package com.seagame.ext.quest.entities;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.Utils;
import com.seagame.ext.quest.QuestObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.index.Indexed;

@Setter
@Getter
@ToString
public class TaskProgress {
    @Indexed
    private boolean claim;
    private boolean active;
    private boolean claimed;
    private boolean seen;
    private int action;
    private String taskKey;
    public int count;
    private int targetCount;
    private long createTime;
    private String deviceToken;
    private String unlockTask;
    private String rewards;

    public TaskProgress() {
    }

    public TaskProgress(int action, String req) {
        initBase(action, req);
    }

    private void initBase(int action, String req) {
        this.action = action;
        createTime = System.currentTimeMillis();
        unlockTask = "";
        rewards = "";
        count = 0;
        if (!Utils.isNullOrEmpty(req)) {
            String[] task = req.split("/");
            if (task.length > 0) {
                this.taskKey = task[0];
            }
            if (task.length > 1) {
                this.targetCount = Integer.parseInt(task[1]);
            }
        }
    }

    public void reset() {
        createTime = System.currentTimeMillis();
        claim = false;
        seen = false;
        count = 0;
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

    public TaskProgress setSeen() {
        this.seen = true;
        return this;
    }

    public boolean incr(Integer value) {
        if (value <= 0)
            return false;
        count += value;
        if (count > targetCount)
            count = targetCount;

//        QAntTracer.debug(this.getClass(), "Task  " + id + " / " + count + " / " + targetCount);
        claim = count >= targetCount;
        return count >= targetCount;
    }

    public boolean updateCount(Integer value) {
        count = value;
        return count >= targetCount;
    }

    public boolean isFinish() {
        return this.action == QuestObserver.TYPE_GIVE || count >= targetCount;
    }

    public boolean isNotFinish() {
        return !this.isFinish();

    }

    public boolean isApplyAble() {
        return this.isActive() && this.isNotFinish();
    }

    public boolean forceComplete() {
        this.count = this.targetCount;
        return true;
    }

    public IQAntObject build() {
        IQAntObject antObject = new QAntObject();
        antObject.putBool("claim", this.claim);
        antObject.putBool("claimed", this.claimed);
        antObject.putBool("seen", this.seen);
        antObject.putBool("active", this.active);
        antObject.putInt("action", this.action);
        if (taskKey != null)
            antObject.putUtfString("taskKey", this.taskKey);
        antObject.putInt("count", this.count);
        antObject.putInt("targetCount", this.targetCount);
        if (this.rewards != null)
            antObject.putUtfString("rewards", this.rewards);
        return antObject;
    }
}
