package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author LamHM
 */
@Getter
@JsonInclude(Include.NON_NULL)
public class Stage {
    @JacksonXmlProperty(localName = "ChapterIndex", isAttribute = true)
    private String ChapterIndex;
    @JacksonXmlProperty(localName = "ChapterName", isAttribute = true)
    private String ChapterName;
    @JacksonXmlProperty(localName = "ChapterBG", isAttribute = true)
    private String ChapterBG;
    @JacksonXmlProperty(localName = "StageIndex", isAttribute = true)
    private String StageIndex;
    @JacksonXmlProperty(localName = "StageName", isAttribute = true)
    private String StageName;
    @JacksonXmlProperty(localName = "UnlockStage", isAttribute = true)
    private String UnlockStage;
    @JacksonXmlProperty(localName = "BattleBG", isAttribute = true)
    private String BattleBG;
    @JacksonXmlProperty(localName = "EnergyCost", isAttribute = true)
    private int EnergyCost;
    @JacksonXmlProperty(localName = "MonsterIndex", isAttribute = true)
    private String MonsterIndex;
    @JacksonXmlProperty(localName = "Formation", isAttribute = true)
    private String Formation;
    @JacksonXmlProperty(localName = "DailyFirstTimeReward", isAttribute = true)
    private String DailyFirstTimeReward;
    @JacksonXmlProperty(localName = "RandomReward", isAttribute = true)
    private String RandomReward;
    @JacksonXmlProperty(localName = "RandomRate", isAttribute = true)
    private String RandomRate;
    @JacksonXmlProperty(localName = "ZenReward", isAttribute = true)
    private String ZenReward;
    @JacksonXmlProperty(localName = "Mode", isAttribute = true)
    private String Mode;
    @JacksonXmlProperty(localName = "Chance", isAttribute = true)
    private int Chance;

    public List<List<String>> monsterWave;

    public void init() {
        monsterWave = new ArrayList<>();
        Arrays.stream(MonsterIndex.split("#")).forEach(s -> {
            List<String> strings = new ArrayList<>(Arrays.asList(s.split(",")));
            monsterWave.add(strings);
        });
    }

}
