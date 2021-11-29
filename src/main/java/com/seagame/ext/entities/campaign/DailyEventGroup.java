package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

/**
 * @author LamHM
 */
@Getter
public class DailyEventGroup {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "EventChance", isAttribute = true)
    private int EventChance;

}
