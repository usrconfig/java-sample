package com.seagame.ext.managers;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ArenaConfig;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.dao.*;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.SystemSetting;
import com.seagame.ext.entities.arena.ArenaHistory;
import com.seagame.ext.entities.arena.ArenaPower;
import com.seagame.ext.entities.arena.RevengeInfo;
import com.seagame.ext.entities.team.BattleTeam;
import com.seagame.ext.entities.team.Team;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.CalculateUtil;
import com.seagame.ext.util.RandomRangeUtil;
import org.apache.commons.lang.math.RandomUtils;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Service
public class ArenaManager extends AbstractExtensionManager implements InitializingBean {
    private static final int MAX_BATTLE_TIME_SECONDS = 300;
    private static final int MAX_SEARCH_RECORD = 3;
    // private static final String ARENA_BONUS_REWARD = "12998/1";
    private static final ArenaConfig arenaConfig = ArenaConfig.getInstance();
    private static final HeroConfig heroConfig = HeroConfig.getInstance();
    private static final ItemConfig itemConfig = ItemConfig.getInstance();

    private static final TriggerKey arenaRankingKey = new TriggerKey("arena" + "_trigger");
    private static final String ARENA_COIN_INDEX = "9999";


    @Value("${arena.opponent.min}")
    private int opponentPowerMin;
    @Value("${arena.opponent.max}")
    private int opponentPowerMax;
    @Value("${arena.opponent.shieldTime}")
    private long shieldTime;

    /**
     * playerId, ArenaPower của đối phương
     */
    private Map<String, Object> battleMap;
    /**
     * gameUserId (đối tượng được search đã trong trận),searchTime
     */
    private Map<String, Long> searchMap;
    private Map<String, Integer> countMap;

    @Autowired
    private ArenaHistoryRepository arenaHistoryRepository;
    @Autowired
    private ArenaPowerRepository arenaPowerRepository;
    @Autowired
    private HeroClassManager heroClassManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private AutoIncrementService autoIncrService;
    @Autowired
    private HeroItemManager heroItemManager;
    @Autowired
    private BattleTeamRepository battleTeamRepo;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private SchedulerFactoryBean schedulerFactory;
    @Autowired
    private SystemSettingRepository systemRepo;
    @Autowired
    private ArenaSearchRepository arenaSearchRepo;

    private SystemSetting systemSetting;

    @Override
    public void afterPropertiesSet() throws Exception {
        countMap = new ConcurrentHashMap<>();
        battleMap = new ConcurrentHashMap<>();
        searchMap = new ConcurrentHashMap<>();
        // TODO sau 1 ngay reset accountMap 1 lan
        systemSetting = systemRepo.findById(SystemSetting.ID).orElse(null);
        if (systemSetting == null) {
            systemSetting = new SystemSetting();
            systemRepo.save(systemSetting);
        }

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                String playerId = "sum1#2030";
//                ArenaPower attacker = getArenaPower(playerId);
//                GameHero gameHeroRequest=playerManager.getGameHero(playerId);
//                List<ArenaPower> opponents = findOpponent(attacker,"sum1");
//            }
//        }, 10);
    }


    public void createNewSeason() {
        systemSetting.newArenaSeason();
        systemRepo.save(systemSetting);
    }

    public long getNextRankingSeconds() {
        return 2592000;
    }

    public ArenaPower update(ArenaPower arenaPower) {
        return arenaPowerRepository.save(arenaPower);
    }


    public void update(List<ArenaPower> arenaPower) {
        arenaPowerRepository.saveAll(arenaPower);
    }


    public void cacheBattle(String gameHeroId, Object info) {
        battleMap.put(gameHeroId, info);
    }

    public ArenaPower fight(QAntUser user, ArenaPower attacker, String defenderId) {

        ArenaPower defender = null;
        defender = getArenaPower(defenderId);
        BattleTeam defBattleTeam = battleTeamRepo.findById(defenderId).orElse(null);
        assert defBattleTeam != null;
        BattleTeam atkBattleTeam = battleTeamRepo.findById(defenderId).orElse(null);
        assert atkBattleTeam != null;

        Team defTeam = defBattleTeam.getDefenceTeam();
        defTeam.setHeroList(heroClassManager.findHeroes(defTeam.getHeroIds(), true));

        Long searchTime = searchMap.get(defender.getPlayerId());
        // nếu đang trong trận đấu thì báo đối phương đang trong trận
        // chiến, refun lai arene ticket
        if (searchTime != null && ((System.currentTimeMillis() - searchTime) / 1000) < MAX_BATTLE_TIME_SECONDS)
            return null;

        if (attacker.hasShield())
            attacker.removeShield();

        searchMap.put(defender.getPlayerId(), System.currentTimeMillis());

        Team atkTeam = atkBattleTeam.getArenaTeam();
        atkTeam.setHeroList(heroClassManager.findHeroes(atkTeam.getHeroIds(), true));
        defender.setOpponent(attacker);

        // trừ điểm nếu thua trước, để tránh tình trạng thoát app
        int attackerTrophyIfLose = 60;
        attacker.setArenaPointBuffer(attackerTrophyIfLose);
        attacker.decrTrophy(attackerTrophyIfLose);

        update(attacker);
        update(defender);

        return defender;
    }

    public void saveHistory(List<ArenaHistory> arenaHistory) {
        arenaHistoryRepository.saveAll(arenaHistory);
    }

    public void registerArena(Player player, BattleTeam battleTeam) {
        ArenaPower arenaPower = new ArenaPower();
        arenaPower.setName(player.getName());
        arenaPower.setZone(player.getZoneName());
        arenaPower.setPlayerId(player.getId());
        arenaPower.setBeginTime(System.currentTimeMillis());

        Team team = battleTeam.getCampaignTeam();
        Team arenaTeam = Team.createArenaTeam(team.getHeroList());
        battleTeam.addTeam(arenaTeam);
        Team defTeam = Team.createDefenceTeam(team.getHeroList());
        battleTeam.addTeam(defTeam);
        battleTeamRepo.save(battleTeam);

        // 10 nam
        arenaPower.setBeginner(true);
        arenaPower.setShieldTime(315360000000L);
        arenaPower.setClaimedSeason(getSeason() - 1);
        update(arenaPower);
    }


    public List<ArenaHistory> getHistoryList(String gameHeroId, int page) {
        List<ArenaHistory> historyList = arenaHistoryRepository.getHistoryList(gameHeroId,
                PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "battleTime")));

        List<ArenaHistory> collect = historyList.stream().filter(arenaHistory -> !arenaHistory.isSeen())
                .map(arenaHistory -> {
                    arenaHistory.setSeen(true);
                    return arenaHistory;
                }).collect(Collectors.toList());
        if (collect.size() > 0) {
            arenaHistoryRepository.saveAll(collect);
            countMap.put(gameHeroId, 0);
        }
        return historyList;
    }


    public ArenaHistory getHistory(String historyId) {
        return arenaHistoryRepository.findById(historyId).orElse(null);
    }


    public List<ArenaHistory> getBattle(long id) {
        return arenaHistoryRepository.getBattle(id);
    }


    public ArenaPower getArenaPower(String playerId) {

        ArenaPower arenaPower = arenaPowerRepository.findById(playerId).orElse(null);
        if (arenaPower == null)
            return null;

        BattleTeam battleTeam = battleTeamRepo.findById(playerId).orElse(null);
        if (battleTeam == null)
            return null;

        arenaPower.setAtkTeam(battleTeam.getArenaTeam());
        arenaPower.setDefTeam(battleTeam.getDefenceTeam());

        if (checkUpdateNewSeason(arenaPower)) {
            update(arenaPower);
            arenaPower.setRank(0);
        } else {
            // update rank present season
            try {
                Player pLayer = playerManager.getPlayer(arenaPower.getPlayerId());
                int rank = arenaPowerRepository.getRank(getSeason(), arenaPower.getArenaPoint(), pLayer.getZoneName());
                arenaPower.setRank(rank);
            } catch (Exception e) {
                arenaPower.setRank(0);
                e.printStackTrace();
            }
        }

        Player player = playerManager.getPlayer(playerId);
        arenaPower.setAvatar(player.getAvatar());
        // TODO update more field for ArenaPower

        return arenaPower;

    }


    private boolean checkUpdateNewSeason(ArenaPower arenaPower) {
        if (arenaPower.getSeason() != getSeason()) {
            updateNewSeason(arenaPower, getSeason());
            return true;
        }
        return false;
    }


    private void updateNewSeason(ArenaPower arenaPower, int newSeason) {
        arenaPower.setSeason(newSeason);
        arenaPower.setRank(0);
    }


    public ArenaPower join(String gameHeroId) {
        ArenaPower arenaPower = getArenaPower(gameHeroId);
        if (arenaPower.checkResetTicket())
            update(arenaPower);

        setRank(arenaPower);
        return arenaPower;
    }


    public ArenaPower getDefenderTeam(String gameHeroId) {
        ArenaPower arenaPower = getArenaPower(gameHeroId);
        Team defTeam = arenaPower.getDefTeam();
        defTeam.setHeroList(heroClassManager.findHeroes(defTeam.getHeroIds(), true));
        return arenaPower;
    }


    public void updateHeroName(String playerId, String accountName) {
        ArenaPower arenaPower = getArenaPower(playerId);
        arenaPower.setName(accountName);
        update(arenaPower);
    }


    public void test(BiFunction<QAntUser, IQAntObject, Integer> assetChangeFunc, QAntUser user, IQAntObject params) {
        assetChangeFunc.apply(user, params);
    }

    public ArenaPower finish(QAntUser user, boolean isWin, IQAntObject params) {

        String playerId = user.getName();
        System.out.println("playerId finish " + playerId);
        System.out.println("battleMap finish " + battleMap);
        Object battleInfo = battleMap.get(playerId);
        battleMap.remove(playerId);
        if (battleInfo instanceof RevengeInfo) {
            RevengeInfo revengeInfo = (RevengeInfo) battleInfo;
            return finishRevenge(user, isWin, params, revengeInfo.getOpponent(), revengeInfo.getHistoryId());
        }

        ArenaPower defender = (ArenaPower) battleInfo;
        // TODO restart server can't lose -> null
        String defenderId = defender.getPlayerId();
        Player userDefence = playerManager.getPlayer(defenderId);
        if (userDefence != null)
            defender.setAccLevel(userDefence.getLevel());

        ArenaPower attacker = defender.getOpponent();
        Player userAttack = playerManager.getPlayer(playerId);
        attacker.setAccLevel(userAttack.getLevel());

        searchMap.remove(defenderId);

        // trả lại điểm đã trừ trc đó
        attacker.incrTrophy(-1 * attacker.arenaPointBuffer);
        attacker.setArenaPointBuffer(0);
        defender.decrTrophy(-1 * defender.arenaPointBuffer);
        defender.setArenaPointBuffer(0);

        int attackerTrophy = 0;
        int defenderTrophy = 0;

        if (isWin) {
            defender.setShieldTime(shieldTime);
            int rewardCoin = 10;
            heroItemManager.addItems(attacker.getPlayerId(), ARENA_COIN_INDEX + "/" + rewardCoin);
            attackerTrophy = CalculateUtil.calcTrophyAttackerWin();
            defenderTrophy = CalculateUtil.calcTrophyDefLose();
            attacker.incrTrophy(attackerTrophy);
            defender.decrTrophy(defenderTrophy);
        } else {
            attackerTrophy = CalculateUtil.calcTrophyAttackerLose();
            defenderTrophy = CalculateUtil.calcTrophyDefWin();
            defender.incrTrophy(defenderTrophy);
            attacker.decrTrophy(attackerTrophy);
        }

        update(defender);

        String script = params.getUtfString("script");

        long battleTime = System.currentTimeMillis();
        List<ArenaHistory> historyList = new ArrayList<>(2);
        long battleId = autoIncrService.genArenaBattleId();
        ArenaHistory attackerHistory = new ArenaHistory(battleId, attacker.getPlayerId());

        attackerHistory.setWin(isWin);
        attackerHistory.setScript(script);
        attackerHistory.setSeen(true);
        attackerHistory.setArenaPoint(attackerTrophy);
        attackerHistory.setAttacker(true);
        attackerHistory.setOpponentInfo(defender.buildInfo().toJson());
        attackerHistory.setOpponent(defender.buildInfo().toJson());
        attackerHistory.setBattleTime(battleTime);
        historyList.add(attackerHistory);

        ArenaHistory defenderHistory = new ArenaHistory(battleId, defenderId);
        defenderHistory.setRevengeStatus(ArenaHistory.REVENGE);
        defenderHistory.setWin(!isWin);
        defenderHistory.setScript(script);
        defenderHistory.setArenaPoint(defenderTrophy);
        defenderHistory.setOpponentInfo(attacker.buildInfo().toJson());
        defenderHistory.setOpponent(attacker.buildInfo().toJson());
        defenderHistory.setBattleTime(battleTime);
        historyList.add(defenderHistory);

        saveHistory(historyList);
        update(attacker);
        params.removeElement("script");
        return attacker;
    }


    private ArenaPower finishRevenge(QAntUser user, boolean isWin, IQAntObject params, ArenaPower defender,
                                     String historyId) {

        ArenaPower attacker = defender.getOpponent();
        ArenaHistory previousHistory = arenaHistoryRepository.findById(historyId).orElse(null);
        assert previousHistory != null;
        previousHistory.setRevengeStatus(0);
        arenaHistoryRepository.save(previousHistory);

        String defenderId = defender.getPlayerId();
        searchMap.remove(defenderId);
        Integer count = countMap.get(defenderId);
        if (count == null)
            count = 0;

        count++;

        int attackerTrophy = 0;
        int defenderTrophy = 0;

        // Cộng lại điểm trừ trc đó, lúc fight
        attacker.incrTrophy(-1 * attacker.arenaPointBuffer);
        attacker.setArenaPointBuffer(0);
        defender.decrTrophy(-1 * defender.arenaPointBuffer);
        defender.setArenaPointBuffer(0);

        if (isWin) {
            attacker.removeShield();
            attackerTrophy = RandomRangeUtil.ranBetween(100, 110);
//            defenderTrophy = -RandomRangeUtil.ranBetween(40, 50);
            attacker.incrTrophy(attackerTrophy);
//            defender.decrTrophy(defenderTrophy);
        } else {
            attackerTrophy = -RandomRangeUtil.ranBetween(40, 50);
//            defenderTrophy = RandomRangeUtil.ranBetween(110, 130);
//            defender.incrTrophy(defenderTrophy);
            attacker.decrTrophy(attackerTrophy);
        }

        update(defender);

        String script = params.getUtfString("script");
        params.removeElement("script");

        long battleTime = System.currentTimeMillis();
        List<ArenaHistory> historyList = new ArrayList<>(2);
        long battleId = autoIncrService.genArenaBattleId();
        ArenaHistory attackerHistory = new ArenaHistory(battleId, attacker.getPlayerId());

        attackerHistory.setWin(isWin);
        attackerHistory.setScript(script);
        attackerHistory.setSeen(true);
        attackerHistory.setArenaPoint(attackerTrophy);
        attackerHistory.setAttacker(true);
        attackerHistory.setOpponentInfo(defender.buildInfo().toJson());
        attackerHistory.setOpponent(defender.buildInfo().toJson());
        attackerHistory.setBattleTime(battleTime);
        historyList.add(attackerHistory);

        ArenaHistory defenderHistory = new ArenaHistory(battleId, defenderId);
        defenderHistory.setRevengeStatus(ArenaHistory.REVENGE_SUCCESS);
        defenderHistory.setWin(!isWin);
        defenderHistory.setScript(script);
        defenderHistory.setArenaPoint(defenderTrophy);
        defenderHistory.setOpponentInfo(attacker.buildInfo().toJson());
        defenderHistory.setOpponent(attacker.buildInfo().toJson());
        defenderHistory.setBattleTime(battleTime);
        historyList.add(defenderHistory);

        saveHistory(historyList);
        update(attacker);
        return attacker;
    }


    public void setRank(ArenaPower arenaPower) {
        Player player = playerManager.getPlayer(arenaPower.getPlayerId());
        arenaPower.setRank(arenaPowerRepository.getRank(getSeason(), arenaPower.getArenaPoint(), player.getZoneName()));
    }


    public int getSeason() {
        return systemSetting.getArenaSeason();
    }

    public List<ArenaPower> findOpponent(ArenaPower attacker, String zone) {

        // tìm người chơi trên hệ thống loại trừ người đang trong trận

        BattleTeam atkBattleTeam = battleTeamRepo.findById(attacker.getPlayerId()).orElse(null);
        if (atkBattleTeam == null) {
            return null;
        }

        Team attackerTeam = atkBattleTeam.getArenaTeam();
        int fromPower = attackerTeam.getSearchPower() * opponentPowerMin / 100;
        int toPower = opponentPowerMax / 100 * attackerTeam.getSearchPower();
        fromPower = 0;
        toPower = Math.max(500, toPower);


//		System.out.println("fromPower " + fromPower);
//		System.out.println("toPower " + toPower);
//		System.out.println("(int) attacker.getArenaPoint() " + (int) attacker.getArenaPoint());
        // count all game hero with max power from fromPower to toPower,
        int rowNo = arenaSearchRepo.countByPower(fromPower, toPower,
                (int) attacker.getArenaPoint(), zone);
        System.out.println("rowNo" + rowNo);
        int randomNum = Math.max(rowNo - 3, 1);
        System.out.println("randomNum" + randomNum);
        // get 3 game hero from random rowNo
        List<ArenaPower> powerList = arenaSearchRepo.findOpponent(fromPower, toPower,
                RandomUtils.nextInt(randomNum), (int) attacker.getArenaPoint(), zone);
        System.out.println("powerList " + powerList);
        List<ArenaPower> updateList = powerList.stream().filter(this::checkUpdateNewSeason)
                .collect(Collectors.toList());

        if (updateList.size() > 0)
            update(updateList);
        Collections.shuffle(powerList);
        List<ArenaPower> opponents = powerList.stream()
                .filter(power -> !searchMap.containsKey(power.getPlayerId())
                        && !power.getPlayerId().equals(attacker.getPlayerId())).limit(3).collect(Collectors.toList());

        return opponents.stream().map(defender -> {
            Player player = playerManager.getPlayer(defender.getPlayerId());
            if (player != null) {
                BattleTeam battleTeam = battleTeamRepo.findById(player.getId()).orElse(null);
                if (battleTeam != null) {
                    Team defenceTeam = battleTeam.getDefenceTeam();
                    defenceTeam.setHeroList(heroClassManager.findHeroes(defenceTeam.getHeroIds(), true));
                    defender.setDefTeam(defenceTeam);
                    defender.setAccLevel(player.getLevel());
                    defender.setAvatar(player.getAvatar());
                }
            }
            return defender;
        }).collect(Collectors.toList());
    }

    public List<ArenaPower> getTopArenaPoint(String zone) {
        List<ArenaPower> result = arenaPowerRepository.getTopArenaPoint(getSeason(), zone,
                PageRequest.of(0, 25, Sort.by(Sort.Direction.DESC, "maxTeamPower")));

        AtomicInteger atomicInteger = new AtomicInteger(1);
        result.forEach(userPower -> userPower.setRank(atomicInteger.getAndIncrement()));

        result.forEach(arenaInfo -> {
            Player player = playerManager.getPlayer(arenaInfo.getPlayerId());
            arenaInfo.setAccLevel(player.getLevel());
            arenaInfo.setAvatar(player.getAvatar());
        });

        return result;
    }
}
