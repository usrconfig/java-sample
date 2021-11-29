package com.seagame.ext.managers;

import com.seagame.ext.config.game.StageConfig;
import com.seagame.ext.dao.HeroChapterRepository;
import com.seagame.ext.dao.HeroStageRepository;
import com.seagame.ext.entities.campaign.HeroChapter;
import com.seagame.ext.entities.campaign.HeroStage;
import com.seagame.ext.entities.campaign.Stage;
import com.seagame.ext.services.AutoIncrementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author LamHM
 */
@Service
public class CampaignManager extends AbstractExtensionManager {
    private static final StageConfig stageConfig = StageConfig.getInstance();
    @Autowired
    private HeroStageRepository stageRepo;

    @Autowired
    private HeroChapterRepository heroChapterRepo;

    @Autowired
    private AutoIncrementService autoIncrService;

    public void registerNewHero(String playerId) {
        Stage firstStage = stageConfig.getFirstStage();
        heroChapterRepo.save(new HeroChapter(playerId, firstStage.getChapterIndex(), firstStage.getMode()));
        stageRepo.save(new HeroStage(autoIncrService.genHeroStageId(), playerId, firstStage));
    }
}
