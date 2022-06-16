package com.seagame.ext.entities.item;

import com.creants.eventhandling.dto.GameAssetDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_DEFAULT)
@JsonIgnoreProperties(value = {"Rank"})
public class EquipBase extends ItemBase implements Serializable {
    @JacksonXmlProperty(localName = "maxRank", isAttribute = true)
    private int maxRank;
    @JacksonXmlProperty(localName = "Slot", isAttribute = true)
    private String slot;
    @JacksonXmlProperty(localName = "OptionType", isAttribute = true)
    private String optionType;
    @JacksonXmlProperty(localName = "HeroClass", isAttribute = true)
    private String heroClass;
    @JacksonXmlProperty(localName = "RefundCost", isAttribute = true)
    private String refundCost;

    private List<EquipRankBase> ranks;

    public GameAssetDTO toGameAsset() {
        GameAssetDTO gameAssetDTO = new GameAssetDTO();
        gameAssetDTO.setAssetId(getId());
        gameAssetDTO.setCategory("MU-Equip");
        gameAssetDTO.setType(Utils.getOTypeItem(getSlot()));
        gameAssetDTO.setAclass(Utils.getOClassEquip(getRank()));
        Map<String, Object> of = new HashMap<>();
        of.put("maxRank", getMaxRank());
        of.put("name", getName());
        of.put("description", getDesc());
        of.put("image_url", getRanks().get(0).getIcon());
        Map<Integer, Object> ranks = new HashMap<>();
        getRanks().forEach(equipRankBase -> {
            Map<String, Object> rank = new HashMap<>();
            rank.put("icon", equipRankBase.getIcon());
            rank.put("maxLevel", equipRankBase.getMaxLevel());
            ranks.put(equipRankBase.getRank(), rank);
            of.put("ranks", ranks);
        });
        gameAssetDTO.setAttribute(of);
        return gameAssetDTO;
    }

    @Override
    public String getType() {
        return "equip";
    }
}
