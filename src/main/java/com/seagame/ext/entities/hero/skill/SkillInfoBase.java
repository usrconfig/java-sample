package com.seagame.ext.entities.hero.skill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SkillInfoBase {
    @JacksonXmlProperty(localName = "SkillList", isAttribute = true)
    private List<SkillBase> SkillList;
    @JacksonXmlProperty(localName = "LevelList", isAttribute = true)
    private List<SkillLevelBase> LevelList;
    @JacksonXmlProperty(localName = "UpgradeList", isAttribute = true)
    private List<SkillUpgradeBase> UpgradeList;
}
