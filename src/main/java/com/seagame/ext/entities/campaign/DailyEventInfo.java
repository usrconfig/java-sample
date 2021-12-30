package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.List;

/**
 * @author LamHM
 */
@Getter
public class DailyEventInfo {
    @JacksonXmlProperty(localName = "DailyChallenges", isAttribute = true)
    private List<DailyEvent> DailyChallenges;

}
