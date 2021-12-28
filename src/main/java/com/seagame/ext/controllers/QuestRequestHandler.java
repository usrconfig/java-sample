package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.QuestConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.quest.HeroQuest;
import com.seagame.ext.entities.quest.QuestBase;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.CollectionTask;
import com.seagame.ext.quest.QuestObserver;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.quest.entities.QuestProgress;
import com.seagame.ext.services.NotifySystem;
import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class QuestRequestHandler extends ZClientRequestHandler {

    public static final int GET_QUEST_INFO = 1;
    private static final int START_QUEST = 2;
    private static final int FINISH_QUEST = 3;
    private static final int CLAIM = 4;

    private QuestSystem questSystem;
    private HeroItemManager heroItemManager;
    private PlayerManager playerManager;


    public QuestRequestHandler() {
        questSystem = ExtApplication.getBean(QuestSystem.class);
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case GET_QUEST_INFO:
                getQuestInfo(user, params);
                break;
            case START_QUEST:
                startQuest(user, params);
                break;
            case FINISH_QUEST:
                finishQuest(user, params);
                break;
            case CLAIM:
                claimReward(user, params);
                break;
            default:
                break;
        }

    }


    private void finishQuest(QAntUser user, IQAntObject params) {
        String name = user.getName();
        HeroQuest heroQuest = questSystem.getOrCreateQuest(name);
        String questID = params.getUtfString("id");
        QuestProgress questProgress = heroQuest.getQuestByIndex(questID);
        if (questProgress != null && questProgress.isFinish() && questProgress.isStarted() && !questProgress.isClaim()) {
            try {
                this.consumeQuestItem(user, params, questProgress);
                questProgress.canClaimNow();
                params.putQAntObject("queststat", questProgress.build());
            } catch (UseItemException e) {
                responseError(user, GameErrorCode.LACK_OF_MATIRIAL);
                return;
            }
        }
        send(params, user);
        questSystem.save(heroQuest);
        questSystem.notifyQuestChange(heroQuest);
    }

    private void consumeQuestItem(QAntUser user, IQAntObject params, QuestProgress questProgress) throws UseItemException {
        String consummeItemss = questProgress.getTasks().stream().filter(taskProgress -> (taskProgress.getAction() == QuestObserver.TYPE_COLLECT_GIVE)).map(taskProgress -> taskProgress.getTaskKey() + "/" + taskProgress.getTargetCount()).collect(Collectors.joining("#"));
        if (Utils.isNullOrEmpty(consummeItemss)) {
            return;
        }
        Collection<HeroItem> heroItem = heroItemManager.useItemWithIndex(user.getName(), consummeItemss);
        heroItemManager.save(heroItem);
        heroItemManager.notifyAssetChange(user, heroItem);
        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItem);
    }

    private void getQuestInfo(QAntUser user, IQAntObject params) {
        HeroQuest heroQuest = getQuestInfo(user.getName());
        params.putQAntObject("quest", heroQuest.buildHeroQuest());
        send(params, user);
    }

    private HeroQuest getQuestInfo(String name) {
        HeroQuest quests = questSystem.getOrCreateQuest(name);
        quests.getProgressMap().values().stream().filter(QuestProgress::isNotSeenYet).forEach(QuestProgress::setSeen);
        questSystem.save(quests);
        return quests;
    }


    private void claimReward(QAntUser user, IQAntObject params) {
        String questIdx = params.getUtfString("id");
        String playerId = user.getName();
        Player player = playerManager.getPlayer(playerId);
        HeroQuest heroQuest = questSystem.getOrCreateQuest(playerId);
        if (heroQuest == null) {
            QAntTracer.warn(this.getClass(), "Bad request! playerId:" + playerId);
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }

        QuestProgress questProgress = heroQuest.getQuestByIndex(questIdx);
        if (questProgress == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        if (!questProgress.isClaim() || questProgress.isClaimed()) {
            responseError(user, GameErrorCode.QUEST_IS_RECEIVED);
            return;
        }
        QuestBase questBase = QuestConfig.getInstance().getQuest(questProgress.getIndex());

        List<HeroItem> heroItems = heroItemManager.addItems(user, questBase.getItemReward());
        QAntTracer.debug(PlayerManager.class, "applyRewards : " + heroItems.toString());
        heroItemManager.notifyAssetChange(user, heroItems);
        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
        Collection<HeroItem> heroItemsRewards = ItemConfig.getInstance().buildRewardsReceipt(params, questBase.getItemReward());
        heroItemsRewards.forEach(heroItem -> questSystem.notifyObservers(CollectionTask.init(user.getName(), heroItem.getIndex(), heroItem.getNo())));
        questProgress.setClaimed(true);
        params.putQAntObject("queststat", questProgress.build());

        boolean isLevelUp = player.expUp(10);
        NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
        notifySystem.notifyExpChange(user.getName(), player.buildLevelInfo(), user);

        send(params, user);
        questSystem.save(heroQuest);
        questSystem.notifyQuestChange(heroQuest);
    }

    private void startQuest(QAntUser user, IQAntObject params) {
        String name = user.getName();
        HeroQuest heroQuest = questSystem.getOrCreateQuest(name);
        String questID = params.getUtfString("id");
        QuestProgress questProgress = heroQuest.getQuestByIndex(questID);
        if (questProgress != null && questSystem.checkForAvaiable(name, heroQuest, questProgress)) {
            questProgress.setStarted(true);
            params.putQAntObject("queststat", questProgress.build());
        } else {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        send(params, user);
        questSystem.save(heroQuest);
        questSystem.notifyQuestChange(heroQuest);
    }


    public static void main(String[] args) {

        Date truncate = DateUtils.truncate(new Date(), Calendar.DATE);
//        String startOfDate = DateFormatUtils.format(truncate, "dd/MM/yyyy HH:mm:ss");
//        System.out.println(startOfDate + "/" + truncate.getTime());
//
//        Date addMilliseconds = DateUtils.addMilliseconds(DateUtils.ceiling(new Date(), Calendar.DATE), -1);
//
//        String endOfDate = DateFormatUtils.format(addMilliseconds, "dd/MM/yyyy HH:mm:ss");
//        System.out.println(endOfDate + "/" + addMilliseconds.getTime());
    }

    @Override
    protected String getHandlerCmd() {
        return CMD_QUEST;
    }
}
