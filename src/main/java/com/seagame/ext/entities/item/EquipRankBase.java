package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class EquipRankBase implements Serializable {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int rank;
    @JacksonXmlProperty(localName = "Icon", isAttribute = true)
    private String icon;
    @JacksonXmlProperty(localName = "maxLevel", isAttribute = true)
    private int maxLevel;
    @JacksonXmlProperty(localName = "baseATK", isAttribute = true)
    private int baseATK;
    @JacksonXmlProperty(localName = "baseMAG", isAttribute = true)
    private int baseMAG;
    @JacksonXmlProperty(localName = "baseHP", isAttribute = true)
    private int baseHP;
    @JacksonXmlProperty(localName = "baseDEF", isAttribute = true)
    private int baseDEF;
    @JacksonXmlProperty(localName = "baseRES", isAttribute = true)
    private int baseRES;
    @JacksonXmlProperty(localName = "baseSPD", isAttribute = true)
    private int baseSPD;
    @JacksonXmlProperty(localName = "baseCRIT", isAttribute = true)
    private int baseCRIT;
    @JacksonXmlProperty(localName = "baseMPR", isAttribute = true)
    private int baseMPR;
    @JacksonXmlProperty(localName = "growthATK", isAttribute = true)
    private int growthATK;
    @JacksonXmlProperty(localName = "growthMAG", isAttribute = true)
    private int growthMAG;
    @JacksonXmlProperty(localName = "growthHP", isAttribute = true)
    private int growthHP;
    @JacksonXmlProperty(localName = "growthDEF", isAttribute = true)
    private int growthDEF;
    @JacksonXmlProperty(localName = "growthRES", isAttribute = true)
    private int growthRES;
    @JacksonXmlProperty(localName = "growthSPD", isAttribute = true)
    private int growthSPD;
    @JacksonXmlProperty(localName = "growthCRIT", isAttribute = true)
    private int growthCRIT;
    @JacksonXmlProperty(localName = "growthMPR", isAttribute = true)
    private int Description;
}
