package com.seagame.ext.entities.campaign;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.config.game.StageConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Getter
@Setter
@Document(collection = "hero-stages")
public class HeroStage implements SerializableQAntType {
    private @Id
    long id;
    public String index;
    public String chapterIndex;
    private String playerId;
    public boolean unlock;
    public int starNo;
    private int chance;
    private long lastestSweepTime;
    private @Transient
    Stage stage;


    public HeroStage() {
    }


    public HeroStage(long id, String playerId, Stage stage) {
        this.id = id;
        this.playerId = playerId;
        this.index = stage.getStageIndex();
        this.chapterIndex = stage.getChapterIndex();
        this.chance = stage.getChance();
        setLastestSweepTime(System.currentTimeMillis());
        setUnlock(true);
    }

    public IQAntObject buildInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putLong("id", id);
        result.putUtfString("idx", index);
        result.putUtfString("playerId", playerId);
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
        if (starNo <= 0) {
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
        setLastestSweepTime(System.currentTimeMillis());
        chance--;
    }

    public void resetSweepTimes() {
        chance = StageConfig.getInstance().getStage(this.index).getChance();
        setLastestSweepTime(System.currentTimeMillis());
    }
}
