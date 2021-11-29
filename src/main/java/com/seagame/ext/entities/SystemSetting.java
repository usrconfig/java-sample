package com.seagame.ext.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Document(collection = "system-setting")
public class SystemSetting {
    public @Transient
    static final String ID = "system-setting";
    private @Id
    String id;
    private ArenaInfo arenaInfo;


    public SystemSetting() {
        id = ID;
        createNewSeason(1);
    }


    public ArenaInfo getArenaInfo() {
        return arenaInfo;
    }


    public void newArenaSeason() {
        arenaInfo.createNewSeason();
    }


    public void createNewSeason(int arenaSeason) {
        this.arenaInfo = new ArenaInfo(arenaSeason);
    }


    public int getArenaSeason() {
        return arenaInfo.getArenaSeason();
    }

    class ArenaInfo {
        private int arenaSeason;
        private long arenaSeasonTime;


        public ArenaInfo(int arenaSeason) {
            this.arenaSeason = arenaSeason;
            this.arenaSeasonTime = System.currentTimeMillis();
        }


        public String getId() {
            return id;
        }


        public int getArenaSeason() {
            return arenaSeason;
        }


        public void createNewSeason() {
            arenaSeason++;
            arenaSeasonTime = System.currentTimeMillis();
        }


        public long getArenaSeasonTime() {
            return arenaSeasonTime;
        }

    }

}
