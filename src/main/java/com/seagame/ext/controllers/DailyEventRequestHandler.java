package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.DailyEventConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.campaign.DailyEvent;
import com.seagame.ext.entities.campaign.HeroDailyEvent;
import com.seagame.ext.entities.campaign.MatchInfo;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.*;
import com.seagame.ext.quest.CollectionTask;
import com.seagame.ext.quest.QuestSystem;

import java.util.Collections;
import java.util.List;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class DailyEventRequestHandler extends ZClientRequestHandler {
    private static final int DAILY_EVENT_INFO = 1;
    private static final int FIGHT = 2;
    private static final int FINISH = 3;

    private static final String FREE_TICKET = "9920";
    private static final String TICKET = "9921";


    private static final ItemConfig itemConfig = ItemConfig.getInstance();
    private static final DailyEventConfig dailyEventConfig = DailyEventConfig.getInstance();

    private MatchManager matchManager;
    private QuestSystem questSystem;
    private HeroItemManager heroItemManager;
    private DailyEventManager dailyEventManager;
    private PlayerManager playerManager;
    private HeroClassManager heroClassManager;


    public DailyEventRequestHandler() {
        matchManager = ExtApplication.getBean(MatchManager.class);
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        dailyEventManager = ExtApplication.getBean(DailyEventManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        questSystem = ExtApplication.getBean(QuestSystem.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case DAILY_EVENT_INFO:
                getEventInfo(user, params);
                break;
            case FIGHT:
                fight(user, params);
                break;
            case FINISH:
                finish(user, params);
                break;
            default:
                break;
        }
    }


    @Override
    protected String getHandlerCmd() {
        return ExtensionEvent.CMD_DAILY_EVENT;
    }


    private void getEventInfo(QAntUser user, IQAntObject params) {
        List<HeroDailyEvent> dailyEvents = dailyEventManager.getDailyEvents(user.getName());
        QAntArray qAntArray = new QAntArray();
        dailyEvents.forEach(heroDailyEvent -> qAntArray.addQAntObject(heroDailyEvent.build()));
        params.putQAntArray("list", qAntArray);
        params.putInt("bonus", dailyEventConfig.getEventBonus());
        send(ExtensionEvent.CMD_DAILY_EVENT, params, user);
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

        HeroDailyEvent heroDailyEvent = dailyEventManager.getDailyEvent(playerId, group);
        if (!params.getBool("win")) {
            send(params, user);
            return;
        }
        String nextStage = DailyEventConfig.getInstance().findNextStage(heroDailyEvent.getStageIdx());
        if (!nextStage.equals("x")) heroDailyEvent.setStageIdx(nextStage);
        dailyEventManager.save(heroDailyEvent);
        processReward(params, user, event);
        params.putQAntObject("eventInfo", heroDailyEvent.build());
        send(params, user);
        try {
            questSystem.notifyObservers(CollectionTask.init(user.getName(), "dailyevent", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processReward(IQAntObject params, QAntUser user, String event) {
        DailyEvent dailyEvent = DailyEventConfig.getInstance().getEvents().get(event);
        String rewards = dailyEvent.getReward();
        List<HeroItem> addItems = heroItemManager.addItems(user, rewards);
        heroItemManager.notifyAssetChange(user, addItems);
        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, addItems);
        ItemConfig.getInstance().buildRewardsReceipt(params, rewards);
    }


    private void fight(QAntUser user, IQAntObject params) {
        String idx = params.getUtfString("idx");
        DailyEvent b = dailyEventConfig.getEvents().get(idx);
        if (b == null) {
            responseError(user, GameErrorCode.STAGE_NOT_FOUND);
            return;
        }

        try {
            HeroItem heroItem = heroItemManager.useItem(user, FREE_TICKET, 1);
            List<HeroItem> items = Collections.singletonList(heroItem);
            heroItemManager.notifyAssetChange(user, items);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, items);
        } catch (UseItemException e) {
            try {
                HeroItem heroItem = heroItemManager.useItem(user, TICKET, 1);
                List<HeroItem> items = Collections.singletonList(heroItem);
                heroItemManager.notifyAssetChange(user, items);
                ItemConfig.getInstance().buildUpdateRewardsReceipt(params, items);
            } catch (UseItemException exception) {
                responseError(user, GameErrorCode.NOT_ENOUGH_TICKET);
                return;
            }
        }


        String playerId = user.getName();
        HeroDailyEvent heroDailyEvent = dailyEventManager.getDailyEvent(playerId, b.getGroup());
        heroDailyEvent.decrChance();
        dailyEventManager.save(heroDailyEvent);
        MatchInfo matchInfo = new MatchInfo(playerId, b.getStage(), b.getGroup());
        IQAntObject buildMatchInfo = matchInfo.buildMatchInfo();
        params.putQAntObject("match", buildMatchInfo);
        send(params, user);
        matchManager.newMatch(playerId, matchInfo);
    }


}
