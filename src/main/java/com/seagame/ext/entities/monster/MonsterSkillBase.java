package com.seagame.ext.entities.monster;

import com.creants.creants_2x.core.util.QAntTracer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObject;
import com.seagame.ext.Utils;
import com.seagame.ext.entities.hero.skill.SkillLevelBase;
import lombok.Getter;
import org.bson.json.JsonParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */

/**
 * @author LamHM
 */
@Getter
@JsonIgnoreProperties(value = {"sk"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonsterSkillBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String Type;
    @JacksonXmlProperty(localName = "Ranged", isAttribute = true)
    private boolean Ranged;
    @JacksonXmlProperty(localName = "soundFX", isAttribute = true)
    private String soundFX;
    @JacksonXmlProperty(localName = "hitFX", isAttribute = true)
    private String hitFX;
    @JacksonXmlProperty(localName = "hitCount", isAttribute = true)
    private String hitCount;
    @JacksonXmlProperty(localName = "hitPosition", isAttribute = true)
    private String hitPosition;
    @JacksonXmlProperty(localName = "Cooldown", isAttribute = true)
    private int Cooldown;
    @JacksonXmlProperty(localName = "Specs", isAttribute = true)
    private String tempFormat;

    private BasicDBObject specs;

    public BasicDBObject getSpecs() {
        try {
            if (this.specs == null && !Utils.isNullOrEmpty(tempFormat)) {
                try {
                    this.specs = Utils.isNullOrEmpty(tempFormat) ? null : BasicDBObject.parse(tempFormat);
                } catch (JsonParseException e) {
                    System.out.println(tempFormat);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return specs;
    }

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
