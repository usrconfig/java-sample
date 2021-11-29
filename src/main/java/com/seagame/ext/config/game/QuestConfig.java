package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.QAntTracer;
import com.seagame.ext.entities.quest.QuestBase;
import com.seagame.ext.quest.entities.QuestInfo;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LamHM
 */
@Getter
public class QuestConfig {
    public static final String QUEST_CONFIG = "quests.xml";

    private static QuestConfig instance;
    // map tất cả các quest
    private Map<String, QuestBase> questMap;
    private QuestInfo questInfo;

    public static QuestConfig getInstance() {
        if (instance == null)
            instance = new QuestConfig();
        return instance;
    }


    private QuestConfig() {
        questMap = new HashMap<>();
        loadQuest();
    }


    public String reload() throws IOException {
        loadQuest();
        QAntTracer.info(this.getClass(), "Have just reload file " + QUEST_CONFIG);
        return writeToJsonFile();
    }


    private void loadQuest() {
//        try {
//            XMLStreamReader sr = SourceFileHelper.getStreamReader(QUEST_CONFIG);
//            XmlMapper mapper = new XmlMapper();
//            QuestInfo questInfo;
//            try {
//                questInfo = mapper.readValue(sr, QuestInfo.class);
//                this.questMap = questInfo.getQuests().stream().collect(Collectors.toMap(QuestBase::getId, questBase -> questBase));
//                this.questInfo = questInfo;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            sr.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    public List<QuestBase> getQuestList() {
        return questInfo.getQuests();
    }

    public List<QuestBase> getQuests() {
        return new ArrayList<>(questMap.values());
    }

    public QuestBase getQuest(String questIndex) {
        return questMap.get(questIndex);
    }

    public String writeToJsonFile() throws IOException {
        return SourceFileHelper.exportJsonFile(this.questInfo, "quests.json");
    }


    public static void main(String[] args) throws IOException {
        QuestConfig.getInstance().writeToJsonFile();
    }

}
