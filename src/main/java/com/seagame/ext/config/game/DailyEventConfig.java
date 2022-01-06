package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.seagame.ext.entities.campaign.DailyEvent;
import com.seagame.ext.entities.campaign.DailyEventInfo;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Getter
public class DailyEventConfig {
    public static final String DAILY_EVENT_CONFIG = "daily_event.xml";
    private static DailyEventConfig instance;
    private Map<String, DailyEvent> events;
    DailyEventInfo dailyEventInfos;
    Map<String, List<DailyEvent>> dailyEventMap;
    private int eventBonus;


    public static DailyEventConfig getInstance() {
        if (instance == null) {
            instance = new DailyEventConfig();
        }

        return instance;
    }


    private DailyEventConfig() {
        eventBonus = 1;
        events = new ConcurrentHashMap<>();
        dailyEventMap = new ConcurrentHashMap<>();
        loadEvent();
    }


    public String reload() throws IOException {
        loadEvent();
        return writeToJsonFile();
    }


    private void loadEvent() {
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(DAILY_EVENT_CONFIG);
            XmlMapper mapper = new XmlMapper();
            dailyEventInfos = mapper.readValue(sr, DailyEventInfo.class);
            dailyEventInfos.getDailyChallenges().forEach(dailyEvent -> {
                dailyEvent.init();
                events.put(dailyEvent.getStage(), dailyEvent);
                addEventMap(dailyEvent);
            });
            sr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEventMap(DailyEvent dailyEvent) {
        String key = dailyEvent.getGroup();
        if (!dailyEventMap.containsKey(key)) {
            dailyEventMap.put(key, new ArrayList<>());
        }
        Collection<DailyEvent> stringCollection = dailyEventMap.get(key);
        stringCollection.add(dailyEvent);
    }

    public String writeToJsonFile() throws IOException {
        List<DBObject> collect = dailyEventMap.keySet().stream().map(s -> {
            DBObject dbObject = new BasicDBObject();
            dbObject.put("group", s);
            dbObject.put("chance", 5);
            dbObject.put("stages", dailyEventMap.get(s));
            return dbObject;
        }).collect(Collectors.toList());

        return SourceFileHelper.exportJsonFile(
                collect,
                "daily_event.json");
    }


    public static void main(String[] args) throws IOException {
        DailyEventConfig.getInstance().writeToJsonFile();
    }

    public boolean checkEvent(String event) {
        return events.containsKey(event);
    }

    public String findNextStage(String stageIdx) {
        return events.get(stageIdx).getNextStage();
    }
}
