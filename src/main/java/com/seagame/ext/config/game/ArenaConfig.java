package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.QAntTracer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.arena.ArenaInfoBase;
import com.seagame.ext.entities.arena.ArenaPower;
import com.seagame.ext.entities.arena.ArenaTopReward;
import com.seagame.ext.util.SourceFileHelper;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamHM
 */
public class ArenaConfig {
    public static final String ARENA_CONFIG = "arena.xml";
    private static ArenaConfig instance;
    private ArenaInfoBase arenaInfo;
    private Map<Integer, ArenaTopReward> weekRewards;
    private Map<Integer, ArenaTopReward> monthRewards;


    public static ArenaConfig getInstance() {
        if (instance == null)
            instance = new ArenaConfig();
        return instance;
    }


    private ArenaConfig() {
        loadArenaInfo();
    }


    public void loadArenaInfo() {
        this.weekRewards = new ConcurrentHashMap<>();
        this.monthRewards = new ConcurrentHashMap<>();
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(ARENA_CONFIG);
            XmlMapper mapper = new XmlMapper();
            sr.next(); // to point to <Unicode>
            arenaInfo = mapper.readValue(sr, ArenaInfoBase.class);
            arenaInfo.getTopWeek().forEach(arenaTopReward -> {
                weekRewards.put(arenaTopReward.getRank(), arenaTopReward);
            });
            arenaInfo.getTopMonth().forEach(arenaTopReward -> {
                monthRewards.put(arenaTopReward.getRank(), arenaTopReward);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String reload() throws IOException {
        loadArenaInfo();
        QAntTracer.info(this.getClass(), "Have just reload file " + ARENA_CONFIG);
        return writeToJsonFile();
    }

    public String writeToJsonFile() throws IOException {
        return SourceFileHelper.exportJsonFile(arenaInfo, "arena.json");
    }


    public String getWeekReward(ArenaPower arenaPower) {
        int rank = arenaPower.getRank();
        ArenaTopReward arenaTopReward = weekRewards.get(rank);
        return arenaTopReward != null ? arenaTopReward.getReward() : "";
    }

    public String getMonthReward(ArenaPower arenaPower) {
        int rank = arenaPower.getRank();
        ArenaTopReward arenaTopReward = monthRewards.get(rank);
        return arenaTopReward != null ? arenaTopReward.getReward() : "";
    }


    public static void main(String[] args) throws Exception {
        ArenaConfig.getInstance().writeToJsonFile();
    }
}
