package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class DailyEvent {
    @JacksonXmlProperty(localName = "Index", isAttribute = true)
    private String Index;
    @JacksonXmlProperty(localName = "GroupIndex", isAttribute = true)
    private String GroupIndex;
    @JacksonXmlProperty(localName = "GroupImage", isAttribute = true)
    private String GroupImage;
    @JacksonXmlProperty(localName = "EventName", isAttribute = true)
    private String EventName;
    @JacksonXmlProperty(localName = "StageIndex", isAttribute = true)
    private String StageIndex;
    @JacksonXmlProperty(localName = "StageName", isAttribute = true)
    private String StageName;
    @JacksonXmlProperty(localName = "BattleBG", isAttribute = true)
    private String BattleBG;
    @JacksonXmlProperty(localName = "MonsterIndex", isAttribute = true)
    private String MonsterIndex;
    @JacksonXmlProperty(localName = "Reward", isAttribute = true)
    private String Reward;
    @JacksonXmlProperty(localName = "Chance", isAttribute = true)
    private int Chance;
    @JacksonXmlProperty(localName = "Duration", isAttribute = true)
    private String Duration;

    public List<List<String>> monsterWave;

    public void init() {
        monsterWave = Arrays.stream(MonsterIndex.split("#")).map(s -> Arrays.asList(s.split(","))).collect(Collectors.toList());
    }
}
