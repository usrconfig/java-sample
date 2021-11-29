package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.QAntTracer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.monster.MonsterBase;
import com.seagame.ext.entities.monster.MonsterSkillBase;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author LamHM
 */
@Getter
public class MonsterConfig {
    public static final String MONSTERS_CONFIG = "monsters.xml";
    public static final String MONSTERS_SKILL_CONFIG = "monster_skills.xml";
    private static MonsterConfig instance;
    private Map<String, MonsterBase> monsterMap;
    private Map<String, MonsterSkillBase> skillBaseMap;


    public static MonsterConfig getInstance() {
        if (instance == null)
            instance = new MonsterConfig();
        return instance;
    }


    private MonsterConfig() {
        loadMonsters();
        loadMonstersSkill();
    }


    private void loadMonsters() {
        Map<String, MonsterBase> monstersMap = new HashMap<>();
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(MONSTERS_CONFIG);
            XmlMapper mapper = new XmlMapper();
            sr.next();
            sr.next();
            MonsterBase monster;
            while (sr.hasNext()) {
                try {
                    monster = mapper.readValue(sr, MonsterBase.class);
                    monster.initString();
                    monstersMap.put(monster.getID(), monster);
                } catch (Exception e) {
                }
            }

            this.monsterMap = monstersMap;

            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMonstersSkill() {
        Map<String, MonsterSkillBase> skillBaseMap = new HashMap<>();
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(MONSTERS_SKILL_CONFIG);
            XmlMapper mapper = new XmlMapper();
            sr.next();
            sr.next();
            MonsterSkillBase monster;
            while (sr.hasNext()) {
                try {
                    monster = mapper.readValue(sr, MonsterSkillBase.class);
                    monster.initString();
                    skillBaseMap.put(monster.getID(), monster);
                } catch (Exception e) {
                }
            }

            this.skillBaseMap = skillBaseMap;

            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MonsterBase getMonster(String code) {
        return monsterMap.get(code);
    }


    public String exportGiftCode() throws Exception {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(monsterMap.values());
    }


    public String reloadMonsters() throws Exception {
        loadMonsters();
        QAntTracer.info(this.getClass(), "Have just reload file: " + MONSTERS_CONFIG);
        return exportGiftCode();
    }

    public void writeToJsonFile() {
        try {
            SourceFileHelper.exportJsonFile(
                    monsterMap.values(), "monsters.json");
            SourceFileHelper.exportJsonFile(
                    skillBaseMap.values(), "monster_skills.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MonsterConfig.getInstance().writeToJsonFile();
    }

}
