package com.seagame.ext.entities.hero.skill;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.Utils;
import com.seagame.ext.entities.hero.HeroRankBase;
import com.seagame.ext.entities.hero.HeroRankBaseOut;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "maxLevel", isAttribute = true)
    private int maxLevel;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String Type;
    @JacksonXmlProperty(localName = "Ranged", isAttribute = true)
    private boolean Ranged;
    @JacksonXmlProperty(localName = "Icon", isAttribute = true)
    private String Icon;
    @JacksonXmlProperty(localName = "soundFX", isAttribute = true)
    private String soundFX;
    @JacksonXmlProperty(localName = "hitFX", isAttribute = true)
    private String hitFX;
    @JacksonXmlProperty(localName = "hitCount", isAttribute = true)
    private String hitCount;
    @JacksonXmlProperty(localName = "hitPosition", isAttribute = true)
    private String hitPosition;

    private List<SkillLevelBase> levels;

    private ArrayList<String> soundFXarr;
    private ArrayList<String> hitFXarr;

    public void initString() {
        soundFXarr = new ArrayList<>();
        hitFXarr = new ArrayList<>();
        if (!Utils.isNullOrEmpty(soundFX))
            soundFXarr.addAll(Arrays.stream(soundFX.split("#")).collect(Collectors.toList()));
        if (!Utils.isNullOrEmpty(hitFX))
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
