package com.seagame.ext.entities.campaign;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.item.HeroItem;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class DailyEvent {
    @JacksonXmlProperty(localName = "Stage", isAttribute = true)
    private String Stage;
    @JacksonXmlProperty(localName = "Group", isAttribute = true)
    private String Group;
    @JacksonXmlProperty(localName = "EventName", isAttribute = true)
    private String EventName;
    @JacksonXmlProperty(localName = "NextStage", isAttribute = true)
    private String NextStage;
    @JacksonXmlProperty(localName = "BattleBackground", isAttribute = true)
    private String BattleBackground;
    @JacksonXmlProperty(localName = "Monster", isAttribute = true)
    private String Monster;
    @JacksonXmlProperty(localName = "Chance", isAttribute = true)
    private String Chance;
    @JacksonXmlProperty(localName = "Reward", isAttribute = true)
    private String Reward;

    public List<String> monsterWave;

    public void init() {
        monsterWave = Arrays.asList(Monster.split(","));
    }

    public List<DBObject> getRewards() {
        List<DBObject> simpleRewardList = new ArrayList<>();
        if (!Utils.isNullOrEmpty(getReward())) {
            Collection<HeroItem> items = ItemConfig.getInstance().splitItemToHeroItem(getReward());
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
