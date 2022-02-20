package com.seagame.ext.quest;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.config.game.QuestConfig;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.controllers.QuestRequestHandler;
import com.seagame.ext.dao.QuestRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.quest.HeroQuest;
import com.seagame.ext.entities.quest.QuestBase;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.managers.AbstractExtensionManager;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.entities.QuestData;
import com.seagame.ext.quest.entities.QuestProgress;
import com.seagame.ext.services.MessageFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author LamHM
 */
@Service
public class QuestSystem extends AbstractExtensionManager implements InitializingBean, ExtensionEvent {

    public static final String CLAN_GROUP = "clan";
    public static final String WORLD_GROUP = "world";
    public static final String DAILY_GROUP = "daily";
    public static final String SPECIAL_GROUP = "special";

    private static final QuestConfig questConfig = QuestConfig.getInstance();

    @Autowired
    private QuestRepository questRepo;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private HeroClassManager heroClassManager;

    private List<QuestObserver> observers;


    @Override
    public void afterPropertiesSet() {
        observers = new ArrayList<>();
        new CollectionTask(this);
        new JoinTask(this);
        new FinishDailyTask(this);
    }


    public void addObserver(QuestObserver o) {
        observers.add(o);
    }

    public void notifyObservers(QuestData questData) {
        observers.forEach(questObs -> {
            boolean testTaskKey = this.testKey(questObs.getTaskType(), questData.getType());
            if (testTaskKey) questObs.update(questData);
        });
    }

    private boolean testKey(int taskType, int type) {
        return taskType == type || (type == QuestObserver.TYPE_COLLECT);
    }


    public void resetDailyQuest(String id) {
        HeroQuest quest = getOrCreateQuest(id);
        quest.resetDaily();
        questRepo.save(quest);
    }


    public HeroQuest resetClanDailyQuest(QAntUser user) {
        return resetClanDailyQuest(user.getName());
    }

    public HeroQuest resetClanDailyQuest(String id) {
        HeroQuest quest = getOrCreateQuest(id);
        Player player = playerManager.getPlayer(id);
        if (quest == null) {
            quest = new HeroQuest(id, player.getActiveHeroId());
        }
        ArrayList<QuestBase> quests = new ArrayList<>(questConfig.getQuestList());
        int level = player.getLevel();
        quest.resetClanDaily(quests, level);
        questRepo.save(quest);
        return quest;
    }

    public HeroQuest getOrCreateQuest(String id) {
        long activeHero = 1;
        int level = 1;
        HeroQuest heroQuest = questRepo.getByPlayerIdAndHeroId(id, activeHero);
        if (heroQuest == null) {
            heroQuest = new HeroQuest(id, activeHero);
            ArrayList<QuestBase> quests = new ArrayList<>(questConfig.getQuestList());
            heroQuest.buildQuestList(quests, level);
            questRepo.save(heroQuest);
        }
        return heroQuest;
    }

    public HeroQuest getOrCreateQuestTest(String id) {
        HeroQuest heroQuest = questRepo.getByPlayerIdAndHeroId(id, 1);
        if (heroQuest == null) {
            heroQuest = new HeroQuest(id, 1);
            heroQuest.buildQuestList(new ArrayList<>(questConfig.getQuestList()), 1);
            questRepo.save(heroQuest);
            QAntTracer.debug(QuestSystem.class, "Create Hero Quest: " + heroQuest.toString());
        }
        return heroQuest;
    }

    public HeroQuest getOrCreateGMQuest() {
        HeroQuest heroQuest = questRepo.getByPlayerIdAndHeroId("gm#quest", 0);
        if (heroQuest == null) {
            heroQuest = new HeroQuest("gm#quest", 0);
            questRepo.save(heroQuest);
        }
        return heroQuest;
    }


    public void save(HeroQuest quest) {
        questRepo.save(quest);
    }


    public void delete(HeroQuest quest) {
        questRepo.delete(quest);
    }

    public void finishQuest(String playerId, Collection<QuestProgress> questFinishList) {
        if (questFinishList.stream().anyMatch(QuestProgress::isDailyQuest)) {
            notifyObservers(FinishDailyTask.init(playerId, "daily"));
        }
        ;
    }

    public void removeGameHeroData(String gameHeroId) {
        this.questRepo.removeAllByPlayerId(gameHeroId);
    }


    public void notifyQuestChange(HeroQuest heroQuest) {
        try {
            QAntUser receiverUser = getUserByName(heroQuest.getPlayerId());
            IQAntObject antObject = new QAntObject();
            antObject.putInt("act", QuestRequestHandler.GET_QUEST_INFO);
            antObject.putQAntObject("quest", heroQuest.buildHeroQuest());
            send(ExtensionEvent.CMD_QUEST, antObject, receiverUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QAntUser getUserByName(String id) {
        return extension.getApi().getUserByName(id);
    }

//    public boolean giveQuest(String id, String taskKey, int targetCount) {
//        QAntUser user = getUserByName(id);
//        if (taskKey == null || targetCount <= 0) {
//            responseError(user, LACK_OF_INFOMATION);
//            return false;
//        }
//        HeroItemManager heroItemManager = ExtApplication.getBean(HeroItemManager.class);
//        Collection<HeroItem> items = heroItemManager.getItemsByIndex(user.getName(), taskKey);
//
//        IQAntObject params = new QAntObject();
//        try {
//            Map<Long, Integer> useMap = new ConcurrentHashMap<>();
//            Collection<HeroItem> useItems = new ArrayList<>();
//            int total = targetCount;
//            for (HeroItem heroItem : items) {
//                if (total > 0) {
//                    heroItem.setNo(Math.min(total, heroItem.getNo()));
//                    total -= heroItem.getNo();
//                    useItems.add(heroItem);
//                }
//            }
//            if (total > 0) {
//                responseError(user, LACK_OF_INFOMATION);
//                return false;
//            }
//            useItems.forEach(heroItem -> useMap.put(heroItem.getId(), heroItem.getNo()));
//            Collection<HeroItem> updateItems = heroItemManager.useItems(user, useMap);
//            ItemConfig.getInstance().buildUseReceipt(params, playerManager, updateItems, null);
//            send(CMD_ITEM, params, user);
//            return true;
//        } catch (UseItemException e) {
//            responseError(user, GameErrorCode.NOT_EXIST_ITEM);
//            return false;
//        }
//    }

    private void responseError(QAntUser user, GameErrorCode error) {
        IQAntObject createErrorMsg = MessageFactory.createErrorMsg(CMD_QUEST, error);
        send("cmd_exception", createErrorMsg, user);
    }

    public boolean checkForAvaiable(String userID, HeroQuest heroQuest, QuestProgress questProgress) {
        Player player = playerManager.getPlayer(userID);
        QuestBase questBase = QuestConfig.getInstance().getQuest(questProgress.getIndex());
//        String reqClearQuestID = questBase.getReqClearQuestID();
//        if (reqClearQuestID != null) {
//            QuestProgress reqQuest = heroQuest.getQuestByIndex(reqClearQuestID);
//            if (reqQuest != null && !reqQuest.isFinish()) {
//                return false;
//            }
//        }
//        return player.getLevel() >= questBase.getCharLevel();
        return true;
    }
}
