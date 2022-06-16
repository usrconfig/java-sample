package com.seagame.ext.entities.hero;

import com.creants.eventhandling.dto.GameAssetDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.Utils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class HeroBase {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "Class", isAttribute = true)
    private String heroClass;
    @JacksonXmlProperty(localName = "ClassTitle", isAttribute = true)
    private String ClassTitle;
    @JacksonXmlProperty(localName = "EnegryCAP", isAttribute = true)
    private int EnegryCAP;
    @JacksonXmlProperty(localName = "Rarity", isAttribute = true)
    private int Rarity;
    @JacksonXmlProperty(localName = "MaxRank", isAttribute = true)
    private int MaxRank;
    @JacksonXmlProperty(localName = "Element", isAttribute = true)
    private String Element;
    @JacksonXmlProperty(localName = "Ranged", isAttribute = true)
    private boolean Ranged;
    @JacksonXmlProperty(localName = "Active", isAttribute = true)
    private String Active;
    @JacksonXmlProperty(localName = "Spine", isAttribute = true)
    private String Spine;
    @JacksonXmlProperty(localName = "soundAct", isAttribute = true)
    private String soundAct;
    @JacksonXmlProperty(localName = "soundHurt", isAttribute = true)
    private String soundHurt;
    @JacksonXmlProperty(localName = "soundFX", isAttribute = true)
    private String soundFX;
    @JacksonXmlProperty(localName = "hitFX", isAttribute = true)
    private String hitFX;
    @JacksonXmlProperty(localName = "hitCount", isAttribute = true)
    private int hitCount;
    @JacksonXmlProperty(localName = "hitPosition", isAttribute = true)
    private String hitPosition;

    private ArrayList<String> soundFXarr;
    private ArrayList<String> hitFXarr;
    private ArrayList<HeroRankBaseOut> ranks;

    public void pushRank(HeroRankBase heroRankBase) {
        if (this.ranks == null) {
            this.ranks = new ArrayList<>();
        }
        this.ranks.add(new HeroRankBaseOut(heroRankBase));
    }

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

    public GameAssetDTO toGameAsset() {
        GameAssetDTO gameAssetDTO = new GameAssetDTO();
        gameAssetDTO.setAssetId(getID());
        gameAssetDTO.setCategory("MU-Hero");
        gameAssetDTO.setAclass(Utils.getOClassHero(getRarity()));
        gameAssetDTO.setType(Utils.getOTypeHero(getHeroClass()));
        Map<String, Object> of = new HashMap<>();
        of.put("name", getName());
        of.put("enegryCap", getEnegryCAP());
        of.put("maxRank", getMaxRank());
        of.put("rarity", getRarity());
        of.put("spine", getSpine());
        of.put("image_url", getID());
        of.put("description", getClassTitle());
        gameAssetDTO.setAttribute(of);
        return gameAssetDTO;
    }
}
