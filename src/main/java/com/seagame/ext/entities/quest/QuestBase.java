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
@JsonIgnoreProperties(value = { "start","startDialog" })
public class QuestBase implements NetworkConstant {
    @JacksonXmlProperty(localName = "QuestID", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String desc;
    @JacksonXmlProperty(localName = "ReqClearQuestID", isAttribute = true)
    private String reqClearQuestID;
    @JacksonXmlProperty(localName = "ReqCharLevel", isAttribute = true)
    private int charLevel;
    @JacksonXmlProperty(localName = "QuestHolder", isAttribute = true)
    private String questHolder;
    @JacksonXmlProperty(localName = "AutoStart", isAttribute = true)
    private String start;
    @JacksonXmlProperty(localName = "TaskType", isAttribute = true)
    private int taskType;
    @JacksonXmlProperty(localName = "TaskRequire", isAttribute = true)
    private String taskRequire;
    @JacksonXmlProperty(localName = "TaskDescription", isAttribute = true)
    private String taskDescription;
    @JacksonXmlProperty(localName = "ReportToNpc", isAttribute = true)
    private String reportToNpc;
    @JacksonXmlProperty(localName = "AcceptMsg", isAttribute = true)
    private String acceptMsg;
    @JacksonXmlProperty(localName = "DenyMsg", isAttribute = true)
    private String denyMsg;
    @JacksonXmlProperty(localName = "ReportMsg", isAttribute = true)
    private String reportMsg;
    @JacksonXmlProperty(localName = "Rewards", isAttribute = true)
    private String rewards;
    @JacksonXmlProperty(localName = "DropList", isAttribute = true)
    private String dropList;
    @JacksonXmlProperty(localName = "EXPReward", isAttribute = true)
    private int expReward;
    @JacksonXmlProperty(localName = "HonorPoint", isAttribute = true)
    private int honorPoint;
    @JacksonXmlProperty(localName = "Group", isAttribute = true)
    private String group;
    @JacksonXmlProperty(localName = "AutoStartDialog", isAttribute = true)
    private String startDialog;

    public boolean isAutoStart(){
        return start.equalsIgnoreCase("TRUE");
    }
    public boolean isAutoStartDialog(){
        return startDialog.equalsIgnoreCase("TRUE");
    }
}
