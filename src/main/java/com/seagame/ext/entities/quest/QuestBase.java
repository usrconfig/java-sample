package com.seagame.ext.entities.quest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public List<DBObject> getRewards() {
        List<DBObject> simpleRewardList = new ArrayList<>();
        if (!Utils.isNullOrEmpty(getItemReward())) {
            Collection<HeroItem> items = ItemConfig.getInstance().splitItemToHeroItem(getItemReward());
            items.forEach(heroItem -> {
                BasicDBObjectBuilder start = BasicDBObjectBuilder.start();
                start.add("id", heroItem.getIndex());
                start.add("equip", heroItem.isEquip());
                start.add("count", heroItem.getNo());
                simpleRewardList.add(start.get());
            });
            return simpleRewardList;
        }
        return null;
    }
}
