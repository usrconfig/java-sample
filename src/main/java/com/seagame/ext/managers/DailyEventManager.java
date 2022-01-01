package com.seagame.ext.managers;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.config.game.DailyEventConfig;
import com.seagame.ext.config.game.GameConfig;
import com.seagame.ext.dao.DailyEventRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.campaign.HeroDailyEvent;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.services.AutoIncrementService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Service
public class DailyEventManager extends AbstractExtensionManager implements InitializingBean {

    private static final DailyEventConfig eventConfig = DailyEventConfig.getInstance();
    private static final GameConfig gameConfig = GameConfig.getInstance();

    private static final int ELH = 11; // 11h
    private static final int NTH = 19; // 19h
    private static final String goldTimeRewardString = "11999/30"; // reward
    private static final String goldTimeRewardTitle = "Reward Gold Time"; // reward-title
    private static final String goldTimeRewardDesc = "User login at 11h or 19h will received reward"; // reward-description

    private static final long TWO_HOURS = 2 * 60 * 60 * 1000;

    private Map<String, List<HeroDailyEvent>> dailyEventCashe;
    private @Autowired
    DailyEventRepository eventRepository;
    private @Autowired
    PlayerManager playerManager;
    @Autowired
    private AutoIncrementService autoIncrService;


    @Override
    public void afterPropertiesSet() throws Exception {
        dailyEventCashe = new HashMap<>();
    }


    private boolean isGoldTime() {
        Calendar now = Calendar.getInstance();
        int hrs = now.get(Calendar.HOUR_OF_DAY);

        if (hrs >= ELH && hrs < ELH + 2)
            return true;

        if (hrs >= NTH && hrs < NTH + 2)
            return true;

        return false;
    }


//	private boolean isReceivedGoldTimeReward(Player gameHero) {
//		if (gameHero == null)
//			return true;
//
//		// Da nhan roi
//		Date lastTimeReceived = gameHero.getGoldTimeReward();
//		if (lastTimeReceived != null && System.currentTimeMillis() - lastTimeReceived.getTime() < TWO_HOURS)
//			return true;
//
//		return false;
//	}


//	private String giftGoldTimeReward() {
//		String reward = goldTimeRewardString;
//		GiftCodeBase codeBase = GiftCodeConfig.getInstance().getGiftCode("ADBONUSGIFT");
//		if (codeBase != null) {
//			reward = codeBase.getRandomRewardString();
//		}
//
//		return reward;
//	}


    public void rewardUserLogin(String gameHeroId) {

//		Player gameHero = playerManager.getGameHero(playerId);
//		if (gameHero != null && isGoldTime() && !isReceivedGoldTimeReward(gameHero)) {
//			Mail mail = new Mail(autoIncrService.genMailId(), playerId, "Admin");
//
//			mail.setTitle(goldTimeRewardTitle);
//			String content = goldTimeRewardDesc;
//			mail.setGiftString(giftGoldTimeReward());
//			mail.setContent(content);
//
//			mailRepo.save(mail);
//			this.send(ExtensionEvent.CMD_NTF, MessageFactory.buildNtfMail(mailRepo.countNtf(gameHero.getId())),
//					gameHero.getId());
//
//			gameHero.goldTimeReward = new Date(System.currentTimeMillis());
//			playerManager.updateGameHero(gameHero);
//		}

    }


    public void rewardUserOnline(Collection<QAntUser> userList) {

//		List<GameHero> updates = userList.stream().map(user -> {
//			Player gameHero = playerManager.getGameHero(user.getName());
//			if (gameHero != null && isGoldTime() && !isReceivedGoldTimeReward(gameHero)) {
//				Mail mail = new Mail(autoIncrService.genMailId(), gameHero.getId(), "Admin");
//
//				mail.setTitle(goldTimeRewardTitle);
//				String content = goldTimeRewardDesc;
//				mail.setGiftString(giftGoldTimeReward());
//				mail.setContent(content);
//
//				mailRepo.save(mail);
//				this.send(ExtensionEvent.CMD_NTF, MessageFactory.buildNtfMail(mailRepo.countNtf(gameHero.getId())),
//						gameHero.getId());
//
//				gameHero.goldTimeReward = new Date(System.currentTimeMillis());
//			}
//
//			return gameHero;
//		}).collect(Collectors.toList());
//
//		playerManager.updateGameHeroes(updates);

    }


    public List<HeroDailyEvent> getDailyEvents(String gameHeroId) {
        Player gameHero = playerManager.getPlayer(gameHeroId);
        String key = gameHeroId + gameHero.getActiveHeroId();
        List<HeroDailyEvent> events = dailyEventCashe.get(key);
        if (events == null) {
            events = eventRepository.getAllEvent(gameHeroId);
            if (events.size() <= 0) {
                events = eventConfig.getDailyEventInfos().getDailyChallenges().stream().map(group -> new HeroDailyEvent(gameHeroId, gameHero.getActiveHeroId(), group)).collect(Collectors.toList());
                eventRepository.saveAll(events);
            }
            dailyEventCashe.put(key, events);
        }
        return events;
    }

    public HeroDailyEvent getDailyEvent(String gameHeroId, String stageIdx) {
        return eventRepository.getEvent(gameHeroId, stageIdx);
    }


    public List<HeroDailyEvent> resetDailyEvents(String userId) {
        Player gameHero = playerManager.getPlayer(userId);
        String key = userId + gameHero.getActiveHeroId();
        List<HeroDailyEvent> events = dailyEventCashe.get(key);
        if (events == null) {
            events = eventRepository.getAllEvent(userId);
            if (events.size() <= 0) {
                events = eventConfig.getDailyEventInfos().getDailyChallenges().stream().map(dailyEvent -> new HeroDailyEvent(userId, gameHero.getActiveHeroId(), dailyEvent)).collect(Collectors.toList());
            }
        }
        events.forEach(HeroDailyEvent::resetChance);

        dailyEventCashe.put(key, events);
        return eventRepository.saveAll(events);
    }


    public HeroDailyEvent save(HeroDailyEvent heroDailyEvent) {
        return eventRepository.save(heroDailyEvent);
    }


    public HeroDailyEvent refresh(HeroDailyEvent heroDailyEvent, String gameHeroId) {
        Player gameHero = playerManager.getPlayer(gameHeroId);
        heroDailyEvent.resetChance();
        save(heroDailyEvent);
        return heroDailyEvent;
    }


    public void send(String cmdName, IQAntObject params, String recipientId) {
        QAntUser user = extension.getApi().getUserByName(recipientId);
        if (user != null)
            send(cmdName, params, user);
    }


    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        System.out.println(hour + "/" + minute);
    }

}
