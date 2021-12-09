package com.seagame.ext.managers;

import com.seagame.ext.config.game.StageConfig;
import com.seagame.ext.dao.HeroCampaignRepository;
import com.seagame.ext.entities.campaign.HeroCampaign;
import com.seagame.ext.entities.campaign.HeroStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author LamHM
 */
@Service
public class CampaignManager extends AbstractExtensionManager {
    private static final StageConfig stageConfig = StageConfig.getInstance();

    @Autowired
    private HeroCampaignRepository heroCampaignRepo;

    public HeroCampaign getOrCreateCampaign(String playerId) {
        Optional<HeroCampaign> byId = heroCampaignRepo.findById(playerId);
        if (byId.isPresent()) {
            return byId.get();
        }
        HeroCampaign heroCampaign = new HeroCampaign(playerId);
        heroCampaign.getStages().add(new HeroStage(playerId, stageConfig.getFirstStage()));
        heroCampaignRepo.save(heroCampaign);
        return heroCampaign;
    }

    public boolean isDailyFirstTime(String idx) {
        return true;
    }

    public HeroCampaign save(HeroCampaign campaign) {
        return heroCampaignRepo.save(campaign);
    }
}
