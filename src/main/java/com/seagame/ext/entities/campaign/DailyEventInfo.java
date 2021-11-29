package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.List;

/**
 * @author LamHM
 */
@Getter
public class DailyEventInfo {
    @JacksonXmlProperty(localName = "EventGroups", isAttribute = true)
    private List<DailyEventGroup> EventGroups;
    @JacksonXmlProperty(localName = "HeroTowers", isAttribute = true)
    private List<DailyEvent> HeroTowers;
    @JacksonXmlProperty(localName = "DailyChallenges", isAttribute = true)
    private List<DailyEvent> DailyChallenges;

}
