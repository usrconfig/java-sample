package com.seagame.ext.quest.entities;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.entities.quest.QuestBase;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestInfo {
    @JacksonXmlProperty(localName = "Quests", isAttribute = true)
    private List<QuestBase> quests;
}