package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.QAntTracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.hero.*;
import com.seagame.ext.util.SourceFileHelper;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author LamHM
 */
public class HeroConfig {
    private static HeroConfig instance;
    public static final String HEROES_CONFIG = "heroes.xml";
    private Map<String, HeroBase> heroes;
    private Map<Integer, LevelBase> levelUps;
    private Map<Integer, LevelBase> rankUps;

    public static HeroConfig getInstance() {
        if (instance == null) {
            instance = new HeroConfig();
        }
        return instance;
    }


    private HeroConfig() {
        loadHeroes();
    }


    private void loadHeroes() {
        heroes = new ConcurrentHashMap<>();
        rankUps = new ConcurrentHashMap<>();
        levelUps = new ConcurrentHashMap<>();
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(HEROES_CONFIG);
            XmlMapper mapper = new XmlMapper();
            HeroInfoBase heroesInfo = mapper.readValue(sr, HeroInfoBase.class);
            heroesInfo.getHeroList().forEach(heroBase -> {
                heroBase.initString();
                heroes.put(heroBase.getID(), heroBase);
            });
            List<HeroRankBase> rankList = heroesInfo.getRankList();
            //TODO build full skill all rank
            rankList.forEach(heroRankBase -> {
                if(heroRankBase.getRank()!=3){
                    rankList.stream().filter(heroRankBase1 -> (heroRankBase1.getRank()==3&&heroRankBase1.getID().equals(heroRankBase.getID()))).limit(1).forEach(heroRankBase1 -> heroRankBase.setSkills(heroRankBase1.getSkills()));
                }
            });
            //
            rankList.forEach(heroRankBase -> heroes.get(heroRankBase.getID()).pushRank(heroRankBase));
            heroesInfo.getLevelUpList().forEach(levelBase -> levelUps.put(levelBase.getID(), levelBase));
            heroesInfo.getRankUpList().forEach(levelBase -> rankUps.put(levelBase.getID(), levelBase));
            sr.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }


    public String reload() throws Exception {
        loadHeroes();
        return writeToJsonFile();
    }


    public HeroBase getHeroBase(String index) {
        if (index.contains("/")) {
            index = index.split("/")[0];
        }
        return heroes.get(index);
    }

    public LevelBase getRankUp(int rank) {
        return rankUps.getOrDefault(rank, null);
    }

    public LevelBase getLevelUp(int level) {
        return levelUps.getOrDefault(level, null);
    }

    public HeroRankBaseOut getHeroRankBase(String index, int rank) {
        if (index.contains("/")) {
            index = index.split("/")[0];
        }
        return heroes.get(index).getRanks().get(Math.max(0, rank - 1));
    }


    public List<HeroBase> getHeroes() {
        return new ArrayList<>(heroes.values());
    }


    public String writeToJsonFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<HeroBase> collect = getHeroes();
        mapper.writeValue(new File("export/heroes.json"), collect);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(collect);
    }


    public static void main(String[] args) throws Exception {
        HeroConfig.getInstance().writeToJsonFile();
        SourceFileHelper.exportJsonFile(getInstance().levelUps.values(), "levelUps.json");
        SourceFileHelper.exportJsonFile(getInstance().rankUps.values(), "rankUps.json");
    }

    public int getMaxLevel(String charIndex, int rank) {
        try {
            return heroes.get(charIndex).getRanks().stream().filter(heroRankBaseOut -> heroRankBaseOut.getRank() == rank).findFirst().get().getMaxLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        QAntTracer.debug(HeroConfig.class, "Check CharIdx / Rank : " + charIndex + " / " + rank);
        return 0;
    }
}
