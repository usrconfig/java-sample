package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

/**
 * @author LamHM
 */
@Getter
public class DailyEvent {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String ID;
    @JacksonXmlProperty(localName = "GroupID", isAttribute = true)
    private String GroupID;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "ReqLevel", isAttribute = true)
    private int ReqLevel;
    @JacksonXmlProperty(localName = "WaveID", isAttribute = true)
    private int WaveID;
    @JacksonXmlProperty(localName = "BattleTime", isAttribute = true)
    private int BattleTime;
    @JacksonXmlProperty(localName = "BattleMap", isAttribute = true)
    private String BattleMap;
    @JacksonXmlProperty(localName = "MonsterID", isAttribute = true)
    private String MonsterID;
    @JacksonXmlProperty(localName = "ExpReward", isAttribute = true)
    private int ExpReward;
    @JacksonXmlProperty(localName = "Reward", isAttribute = true)
    private String Reward;
    @JacksonXmlProperty(localName = "RandomBonus", isAttribute = true)
    private String RandomBonus;
    @JacksonXmlProperty(localName = "DropRate", isAttribute = true)
    private String DropRate;
    @JacksonXmlProperty(localName = "EventGroup", isAttribute = true)
    private String EventGroup;

}
