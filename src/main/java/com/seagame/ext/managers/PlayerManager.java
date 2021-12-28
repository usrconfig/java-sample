package com.seagame.ext.managers;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.config.game.StageConfig;
import com.seagame.ext.controllers.ItemRequestHandler;
import com.seagame.ext.dao.BattleTeamRepository;
import com.seagame.ext.dao.PlayerRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.arena.ArenaPower;
import com.seagame.ext.entities.campaign.HeroCampaign;
import com.seagame.ext.entities.campaign.HeroStage;
import com.seagame.ext.entities.campaign.Stage;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.hero.LevelBase;
import com.seagame.ext.entities.item.HeroConsumeItem;
import com.seagame.ext.entities.item.HeroEquipment;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.team.BattleTeam;
import com.seagame.ext.entities.team.Team;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.quest.CollectionTask;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.services.ServiceHelper;
import com.seagame.ext.util.CalculateUtil;
import com.seagame.ext.util.NetworkConstant;
import com.seagame.ext.util.RandomRangeUtil;
import com.seagame.ext.util.TimeExUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author LamHM
 */
@Service
public class PlayerManager extends AbstractExtensionManager implements InitializingBean, NetworkConstant {
    //    public static final String BEGINNER_ITEMS = String.format("%d/1000000#%d/10000000#%d/1000000#%d/10000000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000", Z_RUBY, Z_GOLD, 9991, 9992, 1100, 1101, 1200, 1201, 1300, 1301, 1400, 1500, 1600);
    static final String DAILY_ITEMS = "ITEM107/3";
    //    static final String DAILY_ITEMS = String.format("%s/2/2#%s/2/2#%s/2/2", "as9908", "as9909", "as9910");
    private static final int SEQUENCE_MONTH = 6;
    public static long serverDay;

    public static final int MAX_MISS_DAILY_REWARDS = 3;

    @Autowired
    private PlayerRepository playerRepo;

    @Autowired
    private BattleTeamRepository battleTeamRepository;

    @Autowired
    private AutoIncrementService autoIncrService;

    @Autowired
    private HeroItemManager heroItemManager;

    @Autowired
    private QuestSystem questSystem;

    @Autowired
    private ArenaManager arenaManager;

    @Autowired
    private CampaignManager campaignManager;

    @Autowired
    private HeroClassManager heroClassManager;

    @Value("${enable.dynamic.event}")
    private boolean dynamicEvent;
    @Value("${enable.addcode.test}")
    private boolean enableCode;

    private Map<String, Player> gameHeroMap;
    private static final Timer timer = new Timer();
    private Map<String, Boolean> monsterRewardsMap;


    @Override
    public void afterPropertiesSet() throws Exception {
        gameHeroMap = new HashMap<>();
        monsterRewardsMap = new ConcurrentHashMap<>();
        PlayerManager.serverDay = autoIncrService.getCurrentDays();
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 5);
//        calendar.set(Calendar.MILLISECOND, 0);
        PlayerManager playerManager = this;
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
//                ItemConfig.getInstance().buildReceivedRewards("im6/8/2#im5/2/1", gameHeroManager, "os1#1001");
//        questSystem.notifyObservers(KillTask.init("TamNhanLang", 10));
//        questSystem.notifyObservers(KillTask.init("nanh", 14));
//                questSystem.notifyObservers(TotalTask.init("os1#1001", "it1"));
//        String completeKey = "Rewards/task6";
//        String[] split = completeKey.split("/");
//        if (split.length == 2) {
//            String action = split[0];
//            String actionKey = split[1];
//            switch (action) {
//                case "Talk":
//                    questSystem.notifyObservers(TalkingTask.init(null, actionKey));
//                    break;
//                case "Task":
//                    questSystem.notifyObservers(TalkingTask.init(null, actionKey));
//                    break;
//                case "Find":
//                    questSystem.notifyObservers(VisitFindTask.init(null, actionKey));
//                    break;
//                case "Rewards":
//                    questSystem.notifyObservers(RewardsTask.init(null, actionKey));
//                    break;
//            }getget
//        }
//                questSystem.notifyObservers(VisitFindTask.init("chest", 1));

//                ShopManager shopManager = ExtApplication.getBean(ShopManager.class);
//                shopManager.getShopPackage("os1#1002", "Shop_Common");
//                String token = MonsterConfig.getInstance().buildMonsterRewards("MONS006", null);
//                String rewards   = null;
//                try {
//                    rewards = MonsterConfig.getInstance().getRewards(token);
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//                QAntTracer.debug(this.getClass(), "applyRewards " + token + "/" + rewards);

//                HeroQuest quests = questSystem.getOrCreateQuestTest("Taolaonet");
//                quests.getProgressMap().values().stream().filter(QuestProgress::isNotSeenYet).forEach(QuestProgress::setSeen);
//                questSystem.save(quests);
//
//                processDailyGift("taolaonet", null, false);

//                questSystem.buildQuestRewards("Taolaonet","MONS005");

//                ShopManager shopManager = ExtApplication.getBean(ShopManager.class);
//                shopManager.getShopPackage("os1#1002", "shop_npc01");
//
//                questSystem.notifyObservers(CollectionTask.init("sk1#1002","login",1));
//                buildHeroTestDefault();
//                Page<HeroClass> heroPage = heroClassManager.getHeroPage("nf1#1001", 1);
//                List<HeroClass> heroes = heroClassManager.findHeroes(heroPage.getContent().stream().limit(5).map(HeroClass::getId).collect(Collectors.toList()), false);

//                String playerId = "nf1#1001";
//                String idx = "cp";//cp/ar/df
//                BattleTeamRepository battleTeamRep=ExtApplication.getBean(BattleTeamRepository.class);
//                BattleTeam battleTeam = battleTeamRep.findById(playerId).orElse(new BattleTeam(playerId));
//                Collection<Long> heroIds = new ArrayList<>();
//                heroIds.add(1001L);
//                heroIds.add(1002L);
//                heroIds.add(1003L);
//Test Team
//                Team oldTeam = battleTeam.getTeam(idx);
//                List<HeroClass> heroes = heroClassManager.findHeroes(heroIds, false);
//                Team team = Team.createTeam(idx, heroes);
//                team.setFormation(heroIds.toArray(new Long[]{}));
//                int leaderIndex = 0;
//                team.setLeaderIndex(leaderIndex);
//                battleTeam.addTeam(team);
//                battleTeamRep.save(battleTeam);
//                team.buildObject();
//                heroes.forEach(heroClass -> heroClass.updateTeam(idx));
//                if (oldTeam != null) {
//                    List<HeroClass> oldHeroes = heroClassManager.findHeroes(oldTeam.getHeroIds().stream().filter(aLong -> !heroIds.contains(aLong)).collect(Collectors.toList()), false);
//                    oldHeroes.forEach(heroClass -> heroClass.removeTeam(idx));
//                    heroes.addAll(oldHeroes);
//                }
//                heroClassManager.save(heroes);

//                String playerId = "nf1" + "#" + 1001;
//                String fullname = "Guest#" + 1001;
//
//                Player player = new Player(playerId, 1001, fullname);
//                player.setDeviceId("");
//                player.setZoneName("nf1");
//                playerManager.updateGameHero(player);
//
//                //Test AreanInfo
//                String playerID="nf1#1001";
//                BattleTeam battleTeam = new BattleTeam(playerID);
//                battleTeam.addTeam(Team.createCampaignTeam(heroClassManager.getHeroes().stream().limit(3).collect(Collectors.toList())));
//                battleTeamRepository.save(battleTeam);
//
//                // mở world, chapter, stage, mission
//                campaignManager.getOrCreateCampaign(playerID);
//
////                 tạo nhiệm vụ cho hero
//
//                arenaManager.registerArena(getPlayer(playerID), battleTeam);
////
//                ArenaManager arenaManager = ExtApplication.getBean(ArenaManager.class);
//                QAntObject params = new QAntObject();
////      get ArenaInfo
//
//                ArenaPower arenaPower = arenaManager.join("nf1#1012");
//                if (arenaPower == null) {
//                    params.putQAntObject("arena", QAntObject.newFromObject(new ArenaPower()));
//                    return;
//                }
//                params.putQAntObject("arena",
//                        arenaPower.getAtkTeam() != null ? arenaPower.buildInfo() : arenaPower.buildNewbieArenaInfo());
//                params.putLong("rankingSeconds", arenaManager.getNextRankingSeconds());
//                QAntTracer.debug(PlayerManager.class,params.getDump());
//                testFindArena();

//                testCampaignInfo();

//                TestLevelUpHero();
//                    buildHeroTestDefault();
//                Math.floorDiv(122, 1000);
//                testOpenEgg();

//                heroItemManager.getTakeOnEquipments("nf1#1001", 1001);

//                testEquip();

//                testPageHero();
            }
        }, 3000, 100000000);

//        String itemIdsOn = "1/2";
//        String[] splitItem = itemIdsOn.split("#");
//        if (splitItem.length > 2) {
//            return;
//        }
//        if (Arrays.stream(splitItem).anyMatch(s -> s.split("/").length != 2)) {
//            return;
//        }
//        Arrays.stream(splitItem).collect(Collectors.toList());

//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                LongStream.range(900, 999).forEach(value -> createBot(value));
//            }
//        }, 3000);
    }

    private void testPageHero() {
        String playerId = "nf1#1001";
        Page<HeroClass> heroPage = heroClassManager.getHeroPage(playerId, 1);
        IQAntArray arr = QAntArray.newInstance();
        heroPage.getContent().forEach(item -> {
            heroClassManager.setHeroBaseAndEquipment(item);
            arr.addQAntObject(item.buildInfo());
        });
    }

    private void testEquip() {
        String playerId = "nf1#1001";
        Collection<Long> ids = new ArrayList<>();
        ids.add((long) 14);
        ids.add((long) 15);
        ids.add((long) 16);
        ids.add((long) 17);
        long heroId = 1001;
        Collection<HeroItem> takeOff = heroItemManager.getTakeOnEquipments(playerId, heroId);
        HeroClass heroClass = heroClassManager.getHeroWithId(playerId, heroId);
        if (heroClass == null) {
            return;
        }
        QAntArray qAntArray = new QAntArray();
        takeOff.stream().filter(heroItem -> !ids.contains(heroItem.getId())).forEach(heroItem -> {
            if (heroItem instanceof HeroEquipment) heroItem.setEquipFor(-1);
            qAntArray.addQAntObject(heroItem.buildInfo());
        });

        Collection<HeroItem> heroItems = heroItemManager.getItemsByIds(playerId, ids);
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroEquipment) heroItem.setEquipFor(heroId);
            qAntArray.addQAntObject(heroItem.buildInfo());
        });
    }

    private void testOpenEgg() {
        Map<String, Integer> refund = new ConcurrentHashMap<>();
        String rewards = RandomRangeUtil.randomDroprate(ItemConfig.getInstance().getEggRewards(), ItemConfig.getInstance().getEggRewardsRate(), 1, 100);
        ItemConfig.getInstance().convertToMap(refund, rewards);
        Collection<HeroItem> items1 = ItemConfig.getInstance().convertToHeroItem(refund);
    }

    private void TestLevelUpHero() {
        long id = 1001;
        HeroClass heroWithId = heroClassManager.getHeroWithId("nf1#1001", id);
        if (heroWithId == null) {
            return;
        }
        int levelMax = HeroConfig.getInstance().getMaxLevel(heroWithId.getCharIndex(), heroWithId.getRank());
        if (heroWithId.getLevel() >= levelMax) {
            return;
        }

        LevelBase levelBase = HeroConfig.getInstance().getLevelUp(heroWithId.getLevel() + 1);

        try {
            String upgradeCost = levelBase.getUpgradeCost();
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex("nf1#1001", upgradeCost);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            return;
        }

        heroWithId.levelUp(1);
        heroClassManager.save(heroWithId);
        heroWithId.buildInfo();
    }

    private void testCampaignInfo() {
        HeroCampaign heroCampaign = campaignManager.getOrCreateCampaign("nf1#1002");
        heroCampaign.build();
    }

    private void testCampaign() {
        HeroCampaign campaign = campaignManager.getOrCreateCampaign("nf1#1001");
        HeroStage heroStage = campaign.getStages().stream().filter(stage -> stage.getIndex().equals("100")).findFirst().get();
        if (heroStage.isFirstClearOrUpdateStar(3)) {
            Stage finishStage = StageConfig.getInstance().getStage("100");
            String[] stageArr = StringUtils.split(finishStage.getUnlockStage(), "#");
            // mở stage mới
            String newStageIndex = stageArr[0];
            if (Utils.isNullOrEmpty(newStageIndex) || newStageIndex.equals("#")) {
                campaignManager.save(campaign);
            }
            Arrays.stream(stageArr).forEach(s -> {
                Stage stage = StageConfig.getInstance().getStage(s);
                if (stage != null)
                    campaign.getStages().add(new HeroStage("nf1#1001", stage));
            });
            campaignManager.save(campaign);
        }
    }

    private void testFindArena() {
        String playerId = "nf1#1001";
        ArenaPower attacker = arenaManager.getArenaPower(playerId);
        PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
        Player playerRequest = playerManager.getPlayer(playerId);
        List<ArenaPower> opponents = arenaManager.findOpponent(attacker, playerRequest.getZoneName());

        IQAntArray opponentArr = QAntArray.newInstance();

        opponents.forEach(arenaPower -> {
            IQAntObject buildArenaInfo = arenaPower.buildInfoWithDef();
            buildArenaInfo.putInt("winPoint",
                    CalculateUtil.calcTrophyAttackerWin());
            buildArenaInfo.putInt("losePoint",
                    CalculateUtil.calcTrophyAttackerLose());
            opponentArr.addQAntObject(buildArenaInfo);
        });

        IQAntObject params = new QAntObject();
        params.putQAntArray("opponents", opponentArr);
    }


//    public void createBot(long id) {
//        String playerId = "bot" + "#" + id;
//        if (playerRepo.findOne(playerId) == null) {
//            String fullname = "Guest#" + id;
//            Player player = new Player(playerId, id, fullname);
//            player.setDeviceId("bot");
//            player.setZoneName("bot");
//            player.setOnline(true);
//            player.setNewUser(false);
//            List<String> values = CharConfig.getInstance().getChars().stream().filter(CharBase::isReady).map(CharBase::getId).collect(Collectors.toList());
//            Collections.shuffle(values);
//            player.setBattleHero(values.stream().findFirst().orElse("3"));
//            playerRepo.save(player);
//            updateGameHero(player);
//        }
//    }

    public boolean isOnline(String gameHeroId) {
        Player player = gameHeroMap.get(gameHeroId);
        return player != null && player.isOnline();
    }

    public Player getPlayer(String gameHeroId) {
        Player player = gameHeroMap.get(gameHeroId);
        if (player == null) {
            player = playerRepo.findPlayerById(gameHeroId);
            gameHeroMap.put(gameHeroId, player);
        }

        return player;
    }

    List<Player> findPlayer(String server, String name) {
        int rowNo = playerRepo.countGameHero(server, name);
        int numPlayer = 20;
        if (rowNo <= 0) {
            rowNo = numPlayer + 1;
        }
        int n = rowNo - numPlayer;
        return playerRepo.listGameHero(server, ".*" + name + ".*", (n <= 0 ? 0 : RandomUtils.nextInt(n)), numPlayer);
    }


    public Map<String, Player> getGameHeroMap() {
        return gameHeroMap;
    }

    public void removePlayerData(String gameHeroId) {
        gameHeroMap.remove(gameHeroId);
        playerRepo.remove(gameHeroId);
    }

    public Player getPlayerByDevice(String deviceId, String zoneName) {
        return playerRepo.findPlayerByDeviceIdAndZoneName(deviceId, zoneName);
    }


    public Player getPlayer(long loginId, String zoneName) {
        Player player = playerRepo.findPlayerByLoginIdAndZoneName(loginId, zoneName);
        if (player == null) {
            return playerRepo.findPlayerByLogin3rdIdAndZoneName(loginId, zoneName);
        }
        return player;
    }


    public boolean isExistName(String gameHeroName) {
        return playerRepo.isExistName(gameHeroName) > 0;
    }


    public Page<Player> getHeroes(int page) {
        return playerRepo.getPlayers(PageRequest.of(page - 1, 10));
    }


    public Player login(QAntUser user, IQAntObject response, Collection<String> itemsUpdate) {
        Player player = getHeroAndCurrencyItem(user);
        player.setOnline(true);

        if (player.isNewUser()) {
            player.setNewUser(false);
            playerRepo.save(player);
            ServiceHelper serviceHelper = ExtApplication.getBean(ServiceHelper.class);
            serviceHelper.createWelcomeNewPlayerMail(player.getId());
            resetItemForNewDay(user);
            buildNewPlayer(user, player);
        }

        //TODO test
//        MailManager mailManager = ExtApplication.getBean(MailManager.class);
//        MailConfig.getInstance().getMailByGroup(NetworkConstant.MAIL_G_NEW_PLAYER)
//                .forEach(mailBase -> mailManager.sendSystemMail(user.getName(), mailBase.getId()));
//        MailConfig.getInstance().getMailByGroup("in_app")
//                .forEach(mailBase -> mailManager.sendSystemMail(user.getName(), mailBase.getId()));
//        MailConfig.getInstance().getMailByGroup("special_bundle")
//                .forEach(mailBase -> mailManager.sendSystemMail(user.getName(), mailBase.getId()));
//        MailConfig.getInstance().getMailByGroup("mt")
//                .forEach(mailBase -> mailManager.sendSystemMail(user.getName(), mailBase.getId()));
//
//        mailManager.sendPrivateMail("Admin", user.getName(), "Admin Mail", ": phần thưởng là vật phẩm kèm theo.", "ITEM003/1#ITEM000/3000");
//        mailManager.sendPrivateMail("Admin", user.getName(), "Admin Mail", ": phần thưởng là vật phẩm kèm theo.", "ITEM003/1000#ITEM000/30");
//        mailManager.sendPrivateMail("Admin", user.getName(), "Admin Mail", ": phần thưởng là vật phẩm kèm theo.", null);
        // sang ngày mới
        boolean newDate = TimeExUtil.isNewDate(player);
        if (newDate) {
            player.setLoginTime(new Date());
            loginNewDate(player);
        }


//        GameSetting gameSetting = settingRepository.findOne(gameHeroId);
//        if (gameSetting != null)
//            user.setLocal(gameSetting.getLocation());

        response.putLong("svTime", System.currentTimeMillis());
        buildAvailableShop(response);
        if (newDate) {
            resetItemForNewDay(user);
            serviceHelper.sendVipRewards(player);
        }
        updateGameHero(player);
        updateFunc(player, response);
        return player;
    }

    private void buildNewPlayer(QAntUser user, Player player) {

        this.buildItemDefault(user, player);
        List<HeroClass> heroClasses = this.buildHeroTestDefault(user);

        // tạo team battle
        BattleTeam battleTeam = new BattleTeam(player.getId());
        battleTeam.addTeam(Team.createCampaignTeam(heroClasses.stream().limit(5).collect(Collectors.toList())));
        battleTeamRepository.save(battleTeam);

        // mở world, chapter, stage, mission
        campaignManager.getOrCreateCampaign(player.getId());

        // tạo nhiệm vụ cho hero
//        questSystem.getOrCreateQuest(player.getId());

        arenaManager.registerArena(player, battleTeam);

        player.setEnergyMax(calMaxEnergy(heroClasses));
        player.setEnergy(player.getEnergyMax());
    }

    private List<HeroClass> buildHeroTestDefault(QAntUser user) {
        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
        List<HeroClass> heroes = new ArrayList<>();
        list.forEach(heroBase -> {
            HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
            heroClass.setPlayerId(user.getName());
            heroClass.setId(autoIncrService.genHeroId());
            heroClass.calcFullPower();
            heroes.add(heroClass);
        });
        heroClassManager.save(heroes);
        return heroes;
    }

    private void buildHeroTestDefault() {
        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
        List<HeroClass> heroes = new ArrayList<>();
        list.forEach(heroBase -> {
            HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
            heroClass.setPlayerId("nf1#1001");
            heroClass.setId(autoIncrService.genHeroId());
            heroes.add(heroClass);
        });
        heroClassManager.save(heroes);
    }

    public void buildItemDefault(QAntUser user, Player player) {
        //TODO for Test
        String gameHeroId = player.getId();
        Collection<HeroItem> collection = new ArrayList<>();
        ItemConfig.getInstance().getItems().forEach(itemBase -> {
            HeroItem heroItem = null;
            switch (itemBase.getType()) {
                case ITEM_TICKET://material
                case ITEM_POINT://material
                case ITEM_REWARDS://material
                case ITEM_MATERIAL://material
                    heroItem = new HeroConsumeItem(itemBase);
                    switch (itemBase.getId()) {
                        case ItemRequestHandler.EGG_PIECE:
                            heroItem.setNo(12345);
                            break;
                        case ItemRequestHandler.EGG:
                            heroItem.setNo(300);
                            break;
                        default:
                            heroItem.setNo(300);
                            break;
                    }
                    collection.add(heroItem);
                    break;
                case ITEM_CURRENCY://material
                    heroItem = new HeroConsumeItem(itemBase);
                    heroItem.setNo(90000);
                    collection.add(heroItem);
                    break;
                default:
                    break;
            }

        });
        ItemConfig.getInstance().getEquipMap().values().forEach(itemBase -> {
            HeroItem heroItem = null;
            heroItem = new HeroEquipment(itemBase);
            heroItem.setNo(90000);
            collection.add(heroItem);
        });
        List<HeroItem> addItems = heroItemManager.addItems(user, collection);

//        List<HeroItem> addItems = heroItemManager.addItems(user, BEGINNER_ITEMS, false);

//        player.setAssetMap(addItems.stream().filter(HeroItem::isCurrencyItem)
//                .collect(Collectors.toMap(HeroItem::getIndex, HeroItem::getNo)));
        QAntTracer.debug(this.
                getClass(), "Create new hero: " + gameHeroId);

    }


    private void updateFunc(Player player, IQAntObject response) {
    }

    private void buildAvailableShop(IQAntObject response) {
//        response.putUtfStringArray("shopIds", ShopConfig.getInstance().getAvaiableShop());
    }

    private void resetItemForNewDay(QAntUser user) {
    }


    public void refreshGifts(int sequence) {
//        int key = SEQUENCE_MONTH;
//        Collection<DailyGiftBase> dailyGiftBases = giftConfig.getDailyGift(1);
//        List<GiftItem> collect = dailyGiftBases.stream().map(dailyGiftBase -> new GiftItem(dailyGiftBase, key))
//                .collect(Collectors.toList());
//        HeroGift heroGift = new HeroGift("master#" + key, 0);
//        heroGift.setDailyGift(new DailyGift());
//        heroGift.resetDailyGift(collect, sequence);
//        heroGift.setJoinDay(sequence);
//        giftEventRepo.save(heroGift);
    }

    public void logout(QAntUser user) {
        Player player = playerRepo.findPlayerById(user.getName());
        player.logout();
        updateGameHero(player);

    }


    private void loginNewDate(Player player) {
        String gameHeroId = player.getId();
        resetDailyFunc(gameHeroId);
        player.setEnergyMax(calMaxEnergy(player));
        player.setEnergy(player.getEnergyMax());
    }

    private int calMaxEnergy(Player player) {
        List<HeroClass> heroClassList = heroClassManager.getHeroes(player.getId());
        return calMaxEnergy(heroClassList);
    }

    private int calMaxEnergy(List<HeroClass> heroClassList) {
        return heroClassList.stream().mapToInt(heroClass -> {
            HeroBase heroBase = HeroConfig.getInstance().getHeroBase(heroClass.getCharIndex());
            if (heroBase != null)
                return heroBase.getEnegryCAP();
            return 0;
        }).sum();
    }

    private void resetDailyFunc(String gameHeroId) {
    }


    private Player getHeroAndCurrencyItem(QAntUser user) {
        String gameHeroId = user.getName();
        Player player = playerRepo.findPlayerById(gameHeroId);
//        if (player != null) {
//            List<HeroItem> currencyItemList = heroItemManager.getConsumableItem(gameHeroId, player.getActiveHeroId());
//            QAntTracer.debug(PlayerManager.class, currencyItemList.toString());
//            player.setAssetMap(currencyItemList.stream()
//                    .collect(Collectors.toMap(HeroItem::getIndex, HeroItem::getNo)));
//        }
        return player;
    }


    public void updateGameHero(Player player) {
        gameHeroMap.put(player.getId(), player);
        playerRepo.save(player);
    }


    public boolean isEnableCode() {
        return enableCode;
    }


    public String getDeviceToken(String gameHeroId) {
        Player player = playerRepo.findPlayerById(gameHeroId);
        if (player == null)
            return null;
        return player.getDeviceToken();
    }


    int countUser() {
        Calendar calStart = new GregorianCalendar();
        calStart.setTime(new Date());
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        return playerRepo.countPlayersByLoginTimeAfter(calStart.getTime());
    }


    public void pushMonsterRewards(String s) {
//        String key = Long.toHexString(System.currentTimeMillis());
        this.monsterRewardsMap.put(s, true);
    }

    public boolean checkMonsterRewards(String s) {
//        String key = Long.toHexString(System.currentTimeMillis());
//        return this.monsterRewardsMap.getOrDefault(s, false);
        return true;
    }

    public void claimMonsterRewards(String s) {
//        String key = Long.toHexString(System.currentTimeMillis());
        this.monsterRewardsMap.put(s, false);
    }

    public Player activeHero(String playerId, long heroId) {
        Player player = gameHeroMap.get(playerId);
        if (player == null) {
            player = playerRepo.findPlayerById(playerId);
            gameHeroMap.put(playerId, player);
        }
        playerRepo.save(player);
        return player;
    }


    public List<String> getOnlineList() {
        return gameHeroMap.values().stream().filter(Player::isOnline).map(Player::getId).collect(Collectors.toList());
    }

    public List<Player> getTopWin() {
        return playerRepo.getTopByWinRate(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "winRate")));
    }

    public List<Player> getTopLevel() {
        return playerRepo.getTopByMaxHeroLevel(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "level").and(Sort.by(Sort.Direction.DESC, "exp"))));

    }

    public List<Player> getTopTrophy() {
        return playerRepo.getTopByTrophy(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "trophy")));
    }

    public List<Player> getTopKill() {
        return playerRepo.getTopByKill(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "kill")));
    }

    public void useEnergy(String name, int no) throws UseItemException {
//        Player player = getPlayer(name);
//        if (!player.useEnergy(no)) {
//            throw new UseItemException();
//        }
//        updateGameHero(player);
    }
}
