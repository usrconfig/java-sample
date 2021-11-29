package com.seagame.ext.entities.hero.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillUpgradeBase {
    @JacksonXmlProperty(localName = "Level", isAttribute = true)
    private int Level;
    @JacksonXmlProperty(localName = "ItemID", isAttribute = true)
    private String ItemID;
    @JacksonXmlProperty(localName = "Count", isAttribute = true)
    private int Count;
}
