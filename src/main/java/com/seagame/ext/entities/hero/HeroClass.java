package com.seagame.ext.entities.hero;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.entities.hero.skill.Skill;
import com.seagame.ext.entities.item.HeroEquipment;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.services.NotifySystem;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@Document(collection = "heroes")
public class HeroClass implements SerializableQAntType, NetworkConstant {
    public @Id
    long id;
    String playerId; // id của player sở hữu
    String charIndex;
    public String name;
    private int exp;
    private int level = 1;
    private int rank = 1;
    private List<Skill> skills;
    private @Transient
    List<HeroItem> equipments;
    private Date loginTime;
    private int power;
    private Set<String> onTeam;


    public HeroClass(String id, int level) {
        this.level = level;
        equipments = new ArrayList<>();
        Date date = new Date(System.currentTimeMillis());
        setLoginTime(date);
        initBaseInfo();
        setCharIndex(id);
    }

    public HeroClass() {
        equipments = new ArrayList<>();
        initBaseInfo();
        Date date = new Date(System.currentTimeMillis());
        setLoginTime(date);
    }

    public HeroClass(String id) {
        equipments = new ArrayList<>();
        initBaseInfo();
        Date date = new Date(System.currentTimeMillis());
        setLoginTime(date);
        setCharIndex(id);
    }

    public void setCharIndex(String charIndex) {
        this.charIndex = charIndex;
        this.initSkills(charIndex);
        this.refreshSkillLevel();
    }

    private void refreshSkillLevel() {


    }

    private void initSkills(String charIndex) {
//        HeroRankBaseOut heroBase = HeroConfig.getInstance().getHeroRankBase(charIndex, this.rank);
        //Test max skill
        HeroRankBaseOut heroBase = HeroConfig.getInstance().getHeroRankBase(charIndex, 3);
        if (heroBase != null && heroBase.getSkills() != null) {
            AtomicInteger atomicInteger = new AtomicInteger();
            heroBase.getSkills().forEach(s -> skills.add(new Skill(s, 1, atomicInteger.getAndIncrement())));
        }
    }


    private QAntArray buildEquipmentInfo() {
        if (equipments != null && equipments.size() > 0) {
            QAntArray equipmentArr = new QAntArray();
            equipments.forEach(heroEquipment -> equipmentArr.addQAntObject(heroEquipment.buildInfo()));
            return equipmentArr;
        }

        return null;
    }

    public void addEquipment(HeroEquipment equipment) {
        equipments.add(equipment);
    }

    private void initBaseInfo() {
        this.skills = new ArrayList<>();
    }


    public void levelUp(int value) {
        level += value;
        levelupCheck();
        calcFullPower();
    }

    public void expUp(int exp) {
        this.exp += exp;
        levelupCheck();
    }

    private void levelupCheck() {
//        LevelBase heroLevelBase = GameConfig.getInstance().getLevelBase(level);
//        if (heroLevelBase != null && exp >= heroLevelBase.getTotalEXP()) {
//            levelUp(1);
//        }
    }

    private void notifyFunc(String reqFunc) {
        NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
        notifySystem.notifyMiniGame(this.getPlayerId(), reqFunc);
    }

    private boolean checkFunc(String reqFunc) {
//        if (Utils.notEmpty(reqFunc)) {
//            return this.funcs.contains(reqFunc);
//        }
        return true;
    }


    public int calcFullPower() {
        int sum = 0;
        try {
            sum = equipments.stream().mapToInt(HeroItem::getPower).sum();
        } catch (Exception e) {

        }

        int power = 0;
        try {
            power = HeroConfig.getInstance().getHeroRankBase(this.charIndex, this.rank).getPower();
        } catch (Exception e) {

        }
        this.power = sum + power;
        return this.power;
    }


    public IQAntObject buildInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putLong("id", id);
        result.putUtfString("playerId", playerId);
        if (name != null)
            result.putUtfString("name", name);
        result.putInt("level", level);
        result.putInt("exp", exp);
        result.putInt("rank", rank);
        result.putLong("power", power);
        if (charIndex != null)
            result.putUtfString("idx", charIndex);
        QAntArray equipmentInfo = buildEquipmentInfo();
        if (equipmentInfo != null) {
            result.putQAntArray("equipments", equipmentInfo);
        }
        if (onTeam != null) {
            result.putUtfStringArray("onTeam", onTeam);
        }
        this.buildSkill(result);
        return result;
    }


    private void buildSkill(IQAntObject result) {
        if (this.skills != null) {
            QAntArray array = new QAntArray();
            this.skills.forEach(skill -> array.addQAntObject(skill.build()));
            result.putQAntArray("skills", array);
        }
    }

    public void rankUp() {
        this.rank++;
        this.levelUp(1);
    }

    public void removeTeam(String idx) {
        if (onTeam == null)
            onTeam = new HashSet<>();
        onTeam.remove(idx);
    }

    public void updateTeam(String idx) {
        if (onTeam == null)
            onTeam = new HashSet<>();
        onTeam.add(idx);
    }
}
