package com.seagame.ext.entities.item;

import com.creants.eventhandling.dto.GameAssetDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObject;
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
@JsonInclude(Include.NON_NULL)
public class ItemBase implements Serializable {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String desc;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String type;
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int rank;
    @JacksonXmlProperty(localName = "Icon", isAttribute = true)
    private String icon;

    private List<RewardBase> rewards;


    public GameAssetDTO toGameAsset() {
        GameAssetDTO gameAssetDTO = new GameAssetDTO();
        gameAssetDTO.setAssetId(getId());
        gameAssetDTO.setCategory("MU-Item");
        gameAssetDTO.setType(Utils.getOTypeItem(getId()));
        Map<String, Object> of = new HashMap<>();
        of.put("name", getName());
        of.put("description", getDesc());
        of.put("rank", getRank());
        of.put("image_url", getIcon());
        gameAssetDTO.setAttribute(of);
        return gameAssetDTO;
    }
}
