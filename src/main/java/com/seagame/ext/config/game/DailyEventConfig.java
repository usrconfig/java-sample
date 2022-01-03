package com.seagame.ext.config.game;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.entities.campaign.DailyEvent;
import com.seagame.ext.entities.campaign.DailyEventInfo;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author LamHM
 */
@Getter
public class DailyEventConfig {
    public static final String DAILY_EVENT_CONFIG = "daily_event.xml";
    private static DailyEventConfig instance;
    private Map<String, DailyEvent> events;
    DailyEventInfo dailyEventInfos;
    Map<String, Map<String, Collection<DailyEvent>>> dailyEventMap;
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
            dailyEventMap.put(key, new ConcurrentHashMap<>());
        }
        Map<String, Collection<DailyEvent>> stringCollectionMap = dailyEventMap.get(key);
        if (!stringCollectionMap.containsKey(dailyEvent.getGroup())) {
            stringCollectionMap.put(dailyEvent.getGroup(), new ArrayList<>());
        }
        stringCollectionMap.get(dailyEvent.getGroup()).add(dailyEvent);
    }

    public String writeToJsonFile() throws IOException {
        return SourceFileHelper.exportJsonFile(
                dailyEventInfos.getDailyChallenges(),
                "daily_event.json");
    }


    public static void main(String[] args) throws IOException {
        DailyEventConfig.getInstance().writeToJsonFile();
    }

    public boolean checkEvent(String event, String group) {
        if (dailyEventMap.containsKey(event)) {
            Map<String, Collection<DailyEvent>> stringCollectionMap = dailyEventMap.get(event);
            if (stringCollectionMap.containsKey(group)) {
                return stringCollectionMap.get(group).size() > 0;
            }
        }
        return false;
    }
}
