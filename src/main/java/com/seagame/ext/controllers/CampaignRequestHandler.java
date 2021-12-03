package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.GameConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.config.game.StageConfig;
import com.seagame.ext.dao.HeroCampaignRepository;
import com.seagame.ext.entities.campaign.HeroCampaign;
import com.seagame.ext.entities.campaign.MatchInfo;
import com.seagame.ext.entities.campaign.Stage;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.*;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.NetworkConstant;
import com.seagame.ext.util.RandomRangeUtil;

import java.util.List;


/**
 * @author LamHa
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class CampaignRequestHandler extends ZClientRequestHandler implements NetworkConstant {
    private static final int CAMPAIGN_INFO = 1;
    private static final int FIGHT = 2;
    private static final int FINISH = 3;
    private static final int RAID = 4;
    private static final int RAIDX5 = 5;


    private MatchManager matchManager;
    private QuestSystem questSystem;
    private HeroClassManager heroManager;
    private AutoIncrementService autoIncrService;
    private HeroItemManager heroItemManager;
    private PlayerManager playerManager;
    private CampaignManager campaignManager;

    private static final StageConfig stageConfig = StageConfig.getInstance();
    private static final ItemConfig itemConfig = ItemConfig.getInstance();
    private static final GameConfig gameConfig = GameConfig.getInstance();

    public CampaignRequestHandler() {
        matchManager = ExtApplication.getBean(MatchManager.class);
        questSystem = ExtApplication.getBean(QuestSystem.class);
        autoIncrService = ExtApplication.getBean(AutoIncrementService.class);
        heroManager = ExtApplication.getBean(HeroClassManager.class);
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        campaignManager = ExtApplication.getBean(CampaignManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer act = params.getInt(KEYI_ACTION);
        if (act == null)
            act = 0;

        switch (act) {
            case CAMPAIGN_INFO:
                campaignInfo(user, params);
                break;
            case FIGHT:
                fight(user, params);
                break;
            case FINISH:
                finish(user, params);
                break;
            case RAID:
                doSweep(user, params, 1);
                break;
            case RAIDX5:
                doSweep(user, params, 5);
                break;
        }

    }

    private void campaignInfo(QAntUser user, IQAntObject params) {
        HeroCampaign heroCampaign = campaignManager.getOrCreateCampaign(user.getName());
        QAntArray qAntArray = new QAntArray();
        heroCampaign.getStages().forEach(heroStage -> qAntArray.addQAntObject(heroStage.buildInfo()));
        params.putQAntArray("list", qAntArray);
        send(params, user);
    }


    private void finish(QAntUser user, IQAntObject params) {
        String playerId = user.getName();
        MatchInfo match = matchManager.getMatch(playerId);
        if (match == null) {
            QAntTracer.warn(this.getClass(), "Request finish match not found!");
            return;
        }
        matchManager.removeMatch(playerId);
        String event = match.getEvent();
        String group = match.getGroup();
        params.putUtfString("event", event);
        params.putUtfString("group", group);
        if (!params.getBool("win")) {
            send(params, user);
            return;
        }
        processReward(params, user, event, group, 1);
        send(params, user);
    }

    private void doSweep(QAntUser user, IQAntObject params, int no) {
        String idx = params.getUtfString("idx");
        try {
            int energyCost = StageConfig.getInstance().getStage(idx).getEnergyCost();
            playerManager.useEnergy(user.getName(), energyCost * no);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_TICKET);
            return;
        }
        processReward(params, user, idx, "cp-sweep", no);
        send(params, user);
    }


    public void processReward(IQAntObject params, QAntUser user, String idx, String group, int no) {
        Stage stage = StageConfig.getInstance().getStage(idx);
        String rewards = RandomRangeUtil.randomRewardV2(stage.getRandomReward(), 1);
        String dailyFirstTimeReward = stage.getDailyFirstTimeReward();
        if (campaignManager.isDailyFirstTime(idx) && !Utils.isNullOrEmpty(dailyFirstTimeReward)) {
            rewards += "#" + dailyFirstTimeReward;
        }
        List<HeroItem> addItems = heroItemManager.addItems(user, rewards);
        heroItemManager.notifyAssetChange(user, addItems);
        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, addItems);
        ItemConfig.getInstance().buildRewardsReceipt(params, rewards);
    }


    private void fight(QAntUser user, IQAntObject params) {
        String idx = params.getUtfString("idx");
        try {
            int energyCost = StageConfig.getInstance().getStage(idx).getEnergyCost();
            playerManager.useEnergy(user.getName(), energyCost);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_ENERGY);
            return;
        }
        String playerId = user.getName();
        MatchInfo matchInfo = new MatchInfo(playerId, idx, "cp");
        IQAntObject buildMatchInfo = matchInfo.buildMatchInfo();
        params.putQAntObject("match", buildMatchInfo);
        send(params, user);
        matchManager.newMatch(playerId, matchInfo);
    }

    @Override
    protected String getHandlerCmd() {
        return ExtensionEvent.CMD_CAMPAIGN;
    }
}
