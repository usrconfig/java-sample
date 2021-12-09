package com.seagame.ext.entities.campaign;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.config.game.StageConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */
@Getter
@Setter
public class HeroStage implements SerializableQAntType {
    private String playerId;
    public String index;
    public String chapterIndex;
    public boolean unlock;
    public int starNo;
    private int chance;
    private long lastSweepTime;


    public HeroStage() {
    }


    public HeroStage(String playerId, Stage stage) {
        this.playerId = playerId;
        this.index = stage.getStageIndex();
        this.chapterIndex = stage.getChapterIndex();
        this.chance = stage.getChance();
        setLastSweepTime(System.currentTimeMillis());
        setUnlock(true);
    }

    public IQAntObject buildInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putUtfString("idx", index);
        result.putUtfString("chapIdx", chapterIndex);
        result.putInt("chance", chance);
        result.putInt("starNo", starNo);
        result.putBool("unlock", unlock);
        return result;
    }


    /**
     * @param starNo
     * @return <code>TRUE</code> vừa clear stage lần đầu. <code>FALSE</code>
     * ngược lại
     */
    public boolean isFirstClearOrUpdateStar(int starNo) {
        boolean unlockNewStage = false;
        if (getStarNo() <= 0) {
            setStarNo(starNo);
            unlockNewStage = true;
        } else {
            if (starNo > this.starNo)
                setStarNo(starNo);
        }
        decrSweepTimes();
        return unlockNewStage;
    }


    private void decrSweepTimes() {
        setLastSweepTime(System.currentTimeMillis());
        chance--;
    }

    public void resetSweepTimes() {
        chance = StageConfig.getInstance().getStage(this.index).getChance();
        setLastSweepTime(System.currentTimeMillis());
    }
}
