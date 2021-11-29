package com.seagame.ext.entities.arena;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

/**
 * @author LamHM
 */
@Getter
public class ArenaTopReward {
    @JacksonXmlProperty(localName = "Rank", isAttribute = true)
    private int rank;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "Reward", isAttribute = true)
    private String reward;
}
