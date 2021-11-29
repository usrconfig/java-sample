package com.seagame.ext.entities.quest;

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
public class TaskBase implements NetworkConstant {
    @JacksonXmlProperty(localName = "TaskID", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String desc;
    @JacksonXmlProperty(localName = "TaskType", isAttribute = true)
    private String taskType;
    @JacksonXmlProperty(localName = "TaskRequire", isAttribute = true)
    private String require;
    @JacksonXmlProperty(localName = "ReportToNpc", isAttribute = true)
    private String npc;
    @JacksonXmlProperty(localName = "ActiveMsgID", isAttribute = true)
    private String activeMsg;
}
