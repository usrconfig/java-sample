package com.seagame.ext.entities.quest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.ToString;

/**
 * @author LamHM
 */
@JsonInclude(Include.NON_NULL)
@Getter
@ToString
@JsonIgnoreProperties(value = {""})
public class QuestBase implements NetworkConstant {
    @JacksonXmlProperty(localName = "Index", isAttribute = true)
    private String Index;
    @JacksonXmlProperty(localName = "Group", isAttribute = true)
    private String Group;
    @JacksonXmlProperty(localName = "TaskType", isAttribute = true)
    private int TaskType;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String Description;
    @JacksonXmlProperty(localName = "Task", isAttribute = true)
    private String Task;
    @JacksonXmlProperty(localName = "TargetCount", isAttribute = true)
    private int TargetCount;
    @JacksonXmlProperty(localName = "Win", isAttribute = true)
    private int Win;
    @JacksonXmlProperty(localName = "ItemReward", isAttribute = true)
    private String ItemReward;
    @JacksonXmlProperty(localName = "Goto", isAttribute = true)
    private String Goto;

    public boolean isAutoStart() {
        return true;
    }
}
