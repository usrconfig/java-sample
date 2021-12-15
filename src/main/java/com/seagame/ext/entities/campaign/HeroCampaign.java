package com.seagame.ext.entities.campaign;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Document(collection = "hero-campaign")
public class HeroCampaign {
    private @Id
    String id;
    List<HeroStage> stages;

    public HeroCampaign() {

    }

    public HeroCampaign(String playerId) {
        this.id = playerId;
        stages = new ArrayList<>();
    }

    public IQAntArray build() {
        IQAntArray iqAntArray = new QAntArray();
        Map<String, List<HeroStage>> listMap = new ConcurrentHashMap<>();
        stages.forEach(heroStage -> {
            List<HeroStage> heroStages = listMap.putIfAbsent(heroStage.getChapterIndex(), new ArrayList<>());
            if (heroStages == null) {
                heroStages = listMap.get(heroStage.getChapterIndex());
            }
            heroStages.add(heroStage);
        });
        listMap.keySet().forEach(s -> {
            QAntArray qAntArray = new QAntArray();
            List<HeroStage> heroStages = listMap.get(s);
            heroStages.forEach(heroStage -> {
                qAntArray.addQAntObject(heroStage.buildInfo());
            });
            QAntObject chapter = new QAntObject();
            chapter.putUtfString("idx", s);
            chapter.putQAntArray("list", qAntArray);
            iqAntArray.addQAntObject(chapter);

        });
        return iqAntArray;
    }
}
