package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.seagame.ext.entities.campaign.Stage;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;
import lombok.Setter;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class StageConfig {
    public static final String STAGE_CONFIG = "stages.xml";
    private static StageConfig instance;
    private CampaignOut campaignOut;
    private Map<String, Stage> stagesMap;
    private Map<String, List<Stage>> chapters;


    public static StageConfig getInstance() {
        if (instance == null) {
            instance = new StageConfig();
        }
        return instance;
    }


    private StageConfig() {
        loadStage();
    }


    public String reload() throws IOException {
        loadStage();
        return writeToJsonFile();
    }


    private void loadStage() {
        try {
            Map<String, Stage> stages = new HashMap<>();
            Map<String, List<Stage>> chapterSet = new ConcurrentHashMap<>();
            XMLStreamReader sr = SourceFileHelper.getStreamReader(STAGE_CONFIG);
            XmlMapper mapper = new XmlMapper();
            sr.next(); // to point to <Stages>
            sr.next();
            Stage stage = null;
            while (sr.hasNext()) {
                try {
                    stage = mapper.readValue(sr, Stage.class);
                    stage.init();
                    stages.put(stage.getStageIndex(), stage);
                    chapterSet.putIfAbsent(stage.getChapterIndex(), new ArrayList<>());
                    chapterSet.get(stage.getChapterIndex()).add(stage);
                } catch (Exception e) {
                }

            }


            this.stagesMap = stages;
            this.chapters = chapterSet;
            campaignOut = new CampaignOut();
            campaignOut.setChapters(this.chapters);
            campaignOut.setStages(this.stagesMap.values());
            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public List<Stage> getStages(String chapterIndex) {
        return chapters.get(chapterIndex);
    }

    public Stage getStage(String index) {
        return stagesMap.get(index);
    }


    public String writeToJsonFile() throws IOException {
        List<DBObject> collect = chapters.keySet().stream().map(s -> {
            DBObject dbObject = new BasicDBObject();
            dbObject.put("chapter", s);
            dbObject.put("stages", chapters.get(s));
            return dbObject;
        }).collect(Collectors.toList());

        return SourceFileHelper.exportJsonFile(
                collect,
                "stages.json");
    }


    public static void main(String[] args) throws IOException {
        StageConfig.getInstance().writeToJsonFile();
    }

    public Stage getFirstStage() {
        return stagesMap.get("100");
    }
}

@Getter
@Setter
class CampaignOut {
    private Collection<Stage> stages;
    private Map<String, List<Stage>> chapters;
}