package com.seagame.ext.entities.campaign;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

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

}
