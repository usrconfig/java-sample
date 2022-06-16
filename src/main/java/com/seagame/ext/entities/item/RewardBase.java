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
public class RewardBase implements Serializable {
    @JacksonXmlProperty(localName = "RewardsID", isAttribute = true)
    private String RewardsID;
    @JacksonXmlProperty(localName = "Rate", isAttribute = true)
    private int Rate;
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Type", isAttribute = true)
    private String Type;
    @JacksonXmlProperty(localName = "Count", isAttribute = true)
    private int Count;
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int Rank;
    @JacksonXmlProperty(localName = "Level", isAttribute = true)
    private int Level;
    @JacksonXmlProperty(localName = "Repeat", isAttribute = true)
    private int Repeat;
}
