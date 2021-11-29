package com.seagame.ext.entities.hero.skill;

import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.config.game.SkillConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@ToString
public class Skill {
    public String index;
    public int slot;
    public int level;
    public int exp;
    @Transient
    private SkillBase skillBase;


    public Skill() {
    }

    public Skill(String index, int level, int slot) {
        this.index = index;
        this.level = level;
        this.slot = slot;
    }

    public SkillBase getSkillBase() {
        if (skillBase == null) {
            skillBase = SkillConfig.getInstance().getSkillMap().get(index);
        }
        return skillBase;
    }

    public void expUp(int value) {
        this.exp += value;
        this.levelUpCheck();
    }


    private void levelUpCheck() {
        if (getSkillBase() != null) {
        }


    }

    private void levelUp(int i) {
        this.level += i;
        levelUpCheck();
    }

    public QAntObject build() {
        QAntObject object = new QAntObject();
        object.putUtfString("index", index);
        object.putInt("slot", slot);
        object.putInt("level", level);
        return object;
    }

}
