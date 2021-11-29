package com.seagame.ext.entities.team;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.util.CalculateUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Transient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LamHM
 */
@Getter
@Setter
public class Team implements SerializableQAntType {
    private TeamType teamType;
    public Long[] heroes;
    private @Transient
    List<HeroClass> heroList;
    public int leaderIndex;
    public int teamPower;
    private int searchPower;
    private int numOfHeroes;
    private int maxHeroRank;


    public Team() {
        heroes = new Long[]{-1L, -1L, -1L, -1L, -1L};
        leaderIndex = 0;
    }


    private Team(TeamType teamType, List<HeroClass> heroes) {
        this.heroes = new Long[]{-1L, -1L, -1L, -1L, -1L};
        leaderIndex = 0;
        this.teamType = teamType;
        heroes.stream().map(HeroClass::getId).forEach(this::addHero);
        heroes.stream().reduce((x, y) -> x.getRank() > y.getRank() ? x : y).ifPresent(heroClass -> setMaxHeroRank(heroClass.getRank()));
        teamPower = CalculateUtil.calcPower(heroes);
        searchPower = CalculateUtil.calcSearchPower(heroes);
        numOfHeroes = 0;
        this.heroList = heroes;
    }


    public void updatePower(List<HeroClass> heroes) {
        if (heroes != null && heroes.size() > 0)
            teamPower = CalculateUtil.calcPower(heroes);
    }




    public static Team createTeam(String idx,List<HeroClass> heroes) {
        switch (TeamType.valueOf(idx)){
            case ARENA:
                return createArenaTeam(heroes);
            case DEFENCE:
                return createDefenceTeam(heroes);
            default:
                return createCampaignTeam(heroes);
        }
    }

    public static Team createCampaignTeam(List<HeroClass> heroes) {
        return new Team(TeamType.CAMPAIGN, heroes);
    }


    public static Team createArenaTeam(List<HeroClass> heroes) {
        return new Team(TeamType.ARENA, heroes);
    }


    public static Team createDefenceTeam(List<HeroClass> heroes) {
        return new Team(TeamType.DEFENCE, heroes);
    }


    public IQAntObject buildObject() {
        QAntObject teamObj = QAntObject.newInstance();
        teamObj.putUtfString("idx", teamType.getCode());
        teamObj.putUtfString("id", teamType.getCode());
        teamObj.putLongArray("heroes", getFormation());
        teamObj.putInt("leaderIndex", getLeaderIndex());
        teamObj.putInt("teamPower", getTeamPower());
        return teamObj;
    }


    public void setTeamPower(int teamPower) {
        this.teamPower = teamPower;
    }


    public int getTeamPower() {
        return teamPower;
    }


    public TeamType getTeamType() {
        return teamType;
    }


    public Long[] getHeroes() {
        return heroes;
    }


    public void setFormation(Long[] heroes) {
        this.heroes = heroes;
        getNumOfHeroes();
    }


    public void addHero(long heroId) {
        for (int i = 0; i < heroes.length; i++) {
            if (heroes[i] < 0) {
                heroes[i] = heroId;
                break;
            }
        }
        getNumOfHeroes();
    }


    public List<Long> getIdList() {
        return Arrays.stream(heroes).collect(Collectors.toList());
    }


    public List<Long> getHeroIds() {
        return Arrays.stream(heroes).filter(id -> id > 0).collect(Collectors.toList());
    }


    public List<Long> getFormation() {
        return Arrays.stream(heroes).collect(Collectors.toList());
    }


    public int getNumOfHeroes() {
        numOfHeroes = Stream.iterate(0, i -> i + 1).limit(heroes.length).mapToInt(i -> {
            if (heroes[i] > 0)
                return 1;
            return 0;
        }).sum();
        return numOfHeroes;
    }


    /**
     * @return the maxHeroRank
     */
    public int getMaxHeroRank() {
        return maxHeroRank;
    }


}
