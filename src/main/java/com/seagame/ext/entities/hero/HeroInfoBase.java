package com.seagame.ext.entities.hero;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class HeroInfoBase {
    @JacksonXmlProperty(localName = "HeroList", isAttribute = true)
    private List<HeroBase> HeroList;
    @JacksonXmlProperty(localName = "RankList", isAttribute = true)
    private List<HeroRankBase> RankList;
    @JacksonXmlProperty(localName = "RankUpList", isAttribute = true)
    private List<LevelBase> RankUpList;
    @JacksonXmlProperty(localName = "LevelUpList", isAttribute = true)
    private List<LevelBase> LevelUpList;
}
