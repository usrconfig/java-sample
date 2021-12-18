package com.seagame.ext.entities.item;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class Items {
    @JacksonXmlProperty(localName = "Items", isAttribute = true)
    private List<ItemBase> items;
    @JacksonXmlProperty(localName = "Rewards", isAttribute = true)
    private List<RewardBase> rewards;
    @JacksonXmlProperty(localName = "Equips", isAttribute = true)
    private List<EquipBase> equips;
    @JacksonXmlProperty(localName = "Ranks", isAttribute = true)
    private List<EquipRankBase> ranks;
    @JacksonXmlProperty(localName = "EggRewards", isAttribute = true)
    private List<EggRewardBase> eggRewards;
}
