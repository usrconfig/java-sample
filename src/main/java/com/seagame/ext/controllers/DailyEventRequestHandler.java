package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.DailyEventConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.campaign.HeroDailyEvent;
import com.seagame.ext.entities.campaign.MatchInfo;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.*;
import com.seagame.ext.quest.CollectionTask;
import com.seagame.ext.quest.QuestSystem;

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
        if (!params.getBool("win")) {
            send(params, user);
            return;
        }
        processReward(params, user, event, group);
        send(params, user);
        try {
            questSystem.notifyObservers(CollectionTask.init(user.getName(), "finish_quest_daily", 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void processReward(IQAntObject params, QAntUser user, String event, String group) {
//        Collection<DailyEvent> eventGroup = DailyEventConfig.getInstance().getEvent(event, group);
//        String rewards = eventGroup.stream().map(dailyEvent -> RandomRangeUtil.randomDroprate(dailyEvent.getRandomBonus(), dailyEvent.getDropRate(), 1, 1000) + NetworkConstant.SEPERATE_OTHER_ITEM + dailyEvent.getReward()).collect(Collectors.joining(NetworkConstant.SEPERATE_OTHER_ITEM));
//        List<HeroItem> addItems = heroItemManager.addItems(user, rewards);
//        heroItemManager.notifyAssetChange(user, addItems);
//        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, addItems);
//        ItemConfig.getInstance().buildRewardsReceipt(params, rewards);
//        int expTotal = eventGroup.stream().mapToInt(DailyEvent::getExpReward).sum();
//        HeroClass heroClass = heroClassManager.getHeroActive(user.getName());
//        if (heroClass != null && expTotal > 0) {
//            boolean levelUp = heroClass.expUp(expTotal);
//            heroClassManager.save(heroClass);
//            NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
//            notifySystem.notifyExpChange(user.getName(), heroClass.buildInfoLevel(levelUp), null);
//        }
//        questSystem.notifyObservers(JoinWinTask.init(user.getName(), event));
    }


    private void fight(QAntUser user, IQAntObject params) {
        String event = params.getUtfString("event");
        String group = params.getUtfString("group");
        boolean b = dailyEventConfig.checkEvent(event, group);
        if (!b) {
            responseError(user, GameErrorCode.STAGE_NOT_FOUND);
            return;
        }
        try {
            dailyEventManager.useChanceDailyEvent(user.getName(), event);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_TICKET);
            return;
        }
        String playerId = user.getName();
        MatchInfo matchInfo = new MatchInfo(playerId, event, group);
        IQAntObject buildMatchInfo = matchInfo.buildMatchInfo();
        params.putQAntObject("match", buildMatchInfo);
        send(params, user);
        matchManager.newMatch(playerId, matchInfo);
    }


}
