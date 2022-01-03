package com.seagame.ext.entities.quest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    @JacksonXmlProperty(localName = "Name", isAttribute = true)
    private String Name;
    @JacksonXmlProperty(localName = "Description", isAttribute = true)
    private String Description;
    @JacksonXmlProperty(localName = "TaskType", isAttribute = true)
    private int TaskType;
    @JacksonXmlProperty(localName = "Task", isAttribute = true)
    private String Task;
    @JacksonXmlProperty(localName = "TargetCount", isAttribute = true)
    private int TargetCount;
    @JacksonXmlProperty(localName = "ItemReward", isAttribute = true)
    private String ItemReward;

    public boolean isAutoStart() {
        return true;
    }

    public String getRewards() {
        if (!Utils.isNullOrEmpty(getItemReward())) {
            return Arrays.stream(getItemReward().split("#")).map(s -> {
                String key = s.split("/")[0];
                if (ItemConfig.getInstance().getEquipMap().containsKey(key)) {
                    return "eq/" + s;
                }
                if (ItemConfig.getInstance().getItemMap().containsKey(key)) {
                    return "co/" + s;
                }
                return s;
            }).collect(Collectors.joining("#"));
        }
        return null;
    }
}
