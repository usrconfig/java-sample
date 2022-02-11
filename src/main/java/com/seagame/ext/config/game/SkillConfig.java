package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.hero.skill.SkillBase;
import com.seagame.ext.entities.hero.skill.SkillInfoBase;
import com.seagame.ext.entities.hero.skill.SkillLevelBase;
import com.seagame.ext.entities.hero.skill.SkillUpgradeBase;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class SkillConfig {
    public static final String SKILL_CONFIG = "skills.xml";

    private static SkillConfig instance;
    private Map<String, SkillBase> skillMap;
    private Map<Integer, List<SkillUpgradeBase>> upgradeBaseMap;
    private SkillInfoBase skills;


    public static SkillConfig getInstance() {
        if (instance == null) {
            instance = new SkillConfig();
        }
        return instance;
    }


    private SkillConfig() {
        loadSkill();
    }


    public String reload() throws IOException {
        loadSkill();
        return writeToJsonFile();
    }


    private void loadSkill() {
        this.upgradeBaseMap = new ConcurrentHashMap<>();
        try {
            Map<String, SkillBase> skillMap = new HashMap<>();
            XMLStreamReader sr = SourceFileHelper.getStreamReader(SKILL_CONFIG);
            XmlMapper mapper = new XmlMapper();
            skills = mapper.readValue(sr, SkillInfoBase.class);
            skills.getUpgradeList().forEach(skillUpgradeBase -> {
                if (!this.upgradeBaseMap.containsKey(skillUpgradeBase.getLevel())) {
                    ArrayList<SkillUpgradeBase> value = new ArrayList<>();
                    this.upgradeBaseMap.put(skillUpgradeBase.getLevel(), value);
                }
                this.upgradeBaseMap.get(skillUpgradeBase.getLevel()).add(skillUpgradeBase);
            });

            Map<String, List<SkillLevelBase>> skillLevels = new ConcurrentHashMap<>();
            skills.getLevelList().forEach(skillLevel -> {
                skillLevel.getSpecs();
                skillLevels.putIfAbsent(skillLevel.getID(), new ArrayList<>());
                skillLevels.get(skillLevel.getID()).add(skillLevel);
            });
            skills.getSkillList().forEach(skillBase -> {
                skillBase.initString();
                if (skillLevels.containsKey(skillBase.getID())) {
                    skillBase.setLevels(skillLevels.get(skillBase.getID()));
                }
                skillMap.put(skillBase.getID(), skillBase);
            });

            sr.close();
            this.skillMap = skillMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String writeToJsonFile() throws IOException {
        return SourceFileHelper.exportJsonFile(new ArrayList<>(skillMap.values().stream().sorted(Comparator.comparing(SkillBase::getID)).collect(Collectors.toList())), "skills.json");
    }

    public static void main(String[] args) throws IOException {
        SkillConfig.getInstance().writeToJsonFile();
    }
}
