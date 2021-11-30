package com.seagame.ext.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

}
