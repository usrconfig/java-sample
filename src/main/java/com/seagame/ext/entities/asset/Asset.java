package com.seagame.ext.entities.asset;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

/**
 * @author LamHM
 */
@Getter
public class Asset {
    @JacksonXmlProperty(localName = "Index", isAttribute = true)
    private String Index;
    @JacksonXmlProperty(localName = "ResChar", isAttribute = true)
    private String ResChar;
    @JacksonXmlProperty(localName = "JsonChar", isAttribute = true)
    private String JsonChar;
    @JacksonXmlProperty(localName = "HurtSound", isAttribute = true)
    private String HurtSound;
    @JacksonXmlProperty(localName = "AttackSound", isAttribute = true)
    private String AttackSound;
    @JacksonXmlProperty(localName = "AttackHitEffect", isAttribute = true)
    private String AttackHitEffect;
    @JacksonXmlProperty(localName = "AttackHit", isAttribute = true)
    private int AttackHit;
    @JacksonXmlProperty(localName = "AttackHitPosition", isAttribute = true)
    private String AttackHitPosition;
    @JacksonXmlProperty(localName = "SkillActiveSound", isAttribute = true)
    private String SkillActiveSound;
    @JacksonXmlProperty(localName = "SkillActiveHitEffect", isAttribute = true)
    private String SkillActiveHitEffect;
    @JacksonXmlProperty(localName = "SkillActiveHit", isAttribute = true)
    private int SkillActiveHit;
    @JacksonXmlProperty(localName = "SkillActiveHitPosition", isAttribute = true)
    private String SkillActiveHitPosition;
    @JacksonXmlProperty(localName = "SkillAutoCastSound", isAttribute = true)
    private String SkillAutoCastSound;
    @JacksonXmlProperty(localName = "SkillAutoCastHitEffect", isAttribute = true)
    private String SkillAutoCastHitEffect;
    @JacksonXmlProperty(localName = "SkillAutoCastHit", isAttribute = true)
    private int SkillAutoCastHit;
    @JacksonXmlProperty(localName = "SkillAutoCastHitPosition", isAttribute = true)
    private String SkillAutoCastHitPosition;

}
