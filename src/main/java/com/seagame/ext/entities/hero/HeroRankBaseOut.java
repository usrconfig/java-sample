package com.seagame.ext.entities.hero;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamHM
 */
@Getter
public class HeroRankBaseOut {
    private int rank;
    private int maxLevel;
    private List<String> skills;
    private Map<String, Integer> stats;

    public HeroRankBaseOut() {
    }

    public HeroRankBaseOut(HeroRankBase heroRankBase) {
        this.rank = heroRankBase.getRank();
        this.maxLevel = heroRankBase.getMaxLevel();
        this.skills = new ArrayList<>();
        skills.addAll(Arrays.asList(heroRankBase.getSkills().split("#")));
        stats = new ConcurrentHashMap<>();
        stats.put("baseATK", heroRankBase.getBaseATK());
        stats.put("baseMAG", heroRankBase.getBaseMAG());
        stats.put("baseHP", heroRankBase.getBaseHP());
        stats.put("baseDEF", heroRankBase.getBaseDEF());
        stats.put("baseRES", heroRankBase.getBaseRES());
        stats.put("baseSPD", heroRankBase.getBaseSPD());
        stats.put("baseCRIT", heroRankBase.getBaseCRIT());
        stats.put("baseMPR", heroRankBase.getBaseMPR());
        stats.put("growthATK", heroRankBase.getGrowthATK());
        stats.put("growthMAG", heroRankBase.getGrowthMAG());
        stats.put("growthHP", heroRankBase.getGrowthHP());
        stats.put("growthDEF", heroRankBase.getGrowthDEF());
        stats.put("growthRES", heroRankBase.getGrowthRES());
        stats.put("growthSPD", heroRankBase.getGrowthSPD());
        stats.put("growthMPR", heroRankBase.getGrowthMPR());
    }

    public int getPower(int level, int rank) {
        float ATK = stats.get("baseATK") + level * stats.get("growthATK");
        float SPD = stats.get("baseSPD") + level * stats.get("growthSPD");
        float CRIT = stats.get("baseCRIT");
        float HP = stats.get("baseHP") + level * stats.get("growthHP");
        float DEF = stats.get("baseDEF") + level * stats.get("growthDEF");
        return (int) ((ATK * (2 + SPD / 100 + CRIT / 100) + HP * (1 + 100 / (1000 + DEF / 2))) * rank);
    }
}
