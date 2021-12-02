package com.seagame.ext.entities.monster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.entities.hero.HeroRankBase;
import com.seagame.ext.entities.hero.HeroRankBaseOut;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */

/**
 * @author LamHM
 */
@Getter
@JsonIgnoreProperties(value = {"sk"})
public class MonsterBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "Spine", isAttribute = true)
    private String Spine;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String Type;
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int Rank;
    @JacksonXmlProperty(localName = "Level", isAttribute = true)
    private int Level;
    @JacksonXmlProperty(localName = "Element", isAttribute = true)
    private String Element;
    @JacksonXmlProperty(localName = "EXPReward", isAttribute = true)
    private int EXPReward;
    @JacksonXmlProperty(localName = "ATK", isAttribute = true)
    private int ATK;
    @JacksonXmlProperty(localName = "MAG", isAttribute = true)
    private int MAG;
    @JacksonXmlProperty(localName = "HP", isAttribute = true)
    private int HP;
    @JacksonXmlProperty(localName = "DEF", isAttribute = true)
    private int DEF;
    @JacksonXmlProperty(localName = "RES", isAttribute = true)
    private int RES;
    @JacksonXmlProperty(localName = "SPD", isAttribute = true)
    private int SPD;
    @JacksonXmlProperty(localName = "CRIT", isAttribute = true)
    private int CRIT;
    @JacksonXmlProperty(localName = "MPR", isAttribute = true)
    private int MPR;
    @JacksonXmlProperty(localName = "Class", isAttribute = true)
    private String monsterClass;
    @JacksonXmlProperty(localName = "Ranged", isAttribute = true)
    private boolean Ranged;
    @JacksonXmlProperty(localName = "Skills", isAttribute = true)
    private String Sk;
    @JacksonXmlProperty(localName = "soundAct", isAttribute = true)
    private String soundAct;
    @JacksonXmlProperty(localName = "soundHurt", isAttribute = true)
    private String soundHurt;
    @JacksonXmlProperty(localName = "soundFX", isAttribute = true)
    private String soundFX;
    @JacksonXmlProperty(localName = "hitFX", isAttribute = true)
    private String hitFX;
    @JacksonXmlProperty(localName = "hitCount", isAttribute = true)
    private String hitCount;
    @JacksonXmlProperty(localName = "hitPosition", isAttribute = true)
    private String hitPosition;

    public String[] getSkills() {
        return getSk().split("#");
    }

    private ArrayList<String> soundFXarr;
    private ArrayList<String> hitFXarr;

    public void initString() {
        soundFXarr = new ArrayList<>();
        hitFXarr = new ArrayList<>();
        soundFXarr.addAll(Arrays.stream(soundFX.split("#")).collect(Collectors.toList()));
        hitFXarr.addAll(Arrays.stream(hitFX.split("#")).collect(Collectors.toList()));
    }

    @JsonIgnore
    private String getSoundFX() {
        return this.soundFX;
    }

    @JsonIgnore
    private String getHitFX() {
        return this.hitFX;
    }

}
