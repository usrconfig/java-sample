package com.seagame.ext.managers;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.creants.eventhandling.dto.GameAssetDTO;
import com.creants.eventhandling.dto.UpdateAssetRequest;
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
import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.entities.team.BattleTeam;
import com.seagame.ext.entities.team.Team;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.offchain.IApplyAssets;
import com.seagame.ext.offchain.IGenReward;
import com.seagame.ext.offchain.entities.WolAsset;
import com.seagame.ext.offchain.entities.WolAssetCheckStockRes;
import com.seagame.ext.offchain.entities.WolPlayerRes;
import com.seagame.ext.offchain.entities.WolRewardPlayer;
import com.seagame.ext.offchain.services.AssetMappingManager;
import com.seagame.ext.offchain.services.OffChainServices;
import com.seagame.ext.offchain.services.WolFlowManager;
import com.seagame.ext.quest.JoinTask;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.services.NotifySystem;
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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * @author LamHM
 */
@Service
public class PlayerManager extends AbstractExtensionManager implements InitializingBean, NetworkConstant {
    //    public static final String BEGINNER_ITEMS = String.format("%d/1000000#%d/10000000#%d/1000000#%d/10000000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000#%d/5000", Z_RUBY, Z_GOLD, 9991, 9992, 1100, 1101, 1200, 1201, 1300, 1301, 1400, 1500, 1600);
    static final String DAILY_ITEMS = "9920/1";
    //    static final String DAILY_ITEMS = String.format("%s/2/2#%s/2/2#%s/2/2", "as9908", "as9909", "as9910");
    private static final int SEQUENCE_MONTH = 6;
    public static long serverDay;


    public static final String IN_GAME = "in_game";
    public static final String NFT = "nft";
    public static final String REWARD = "reward";


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
    private DailyEventManager dailyEventManager;

    @Autowired
    private HeroClassManager heroClassManager;

    @Autowired
    private OffChainServices offChainServices;
    @Autowired
    private WolFlowManager wolFlowManager;


    @Autowired
    private AssetMappingManager assetMappingManager;

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

//                testDailyEvent();
//                sendAssetRequest("x000001");
//                sendRewardRequest();
//                offChainServices.rewardFlow();
//                offChainServices.upgradeFlow();
//                //flow open assets
//                try {
//                    WolPlayerRes x000001 = offChainServices.getBalance("x000001");
//                    WolAssetCheckStockReq wolAssetCheckStockReq = new WolAssetCheckStockReq();
//                    WolPlayerRes wolPlayerRes = new WolPlayerRes();
//                    wolPlayerRes.setAddress("x000001");
//                    wolPlayerRes.setKen(x000001.getKen());
//                    wolPlayerRes.setWol(x000001.getWol());
//                    wolAssetCheckStockReq.setPlayer(wolPlayerRes);
//
//                    ArrayList<WolAsset> assets = new ArrayList<>();
//                    WolAsset e = new WolAsset();
//                    e.setAsset_id("test_id_of_game");
//                    e.setStatus("in_game");
//                    e.setCategory("hero");
//                    e.setType("dark_knight");
//                    e.setAclass("b_rank");
//                    e.setGame(OffChainServices.GAME_MU);
//                    assets.add(e);
//                    wolAssetCheckStockReq.setAssets(assets);
//                    WolAssetCheckStockRes wolAssetCheckStockRes = offChainServices.checkStockAsset(wolAssetCheckStockReq);
//
//                    WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
//                    wolAssetCompletedReq.setPlayer(wolAssetCheckStockRes.getPlayer());
//                    wolAssetCompletedReq.setAssets(wolAssetCheckStockRes.getAssets());
//                    WolAssetCompletedRes wolAssetCompletedRes1 = offChainServices.assetCompleted(wolAssetCompletedReq);
//                    WolAssetCompletedRes wolAssetCompletedRes = wolAssetCompletedRes1;
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                //flow rewards
//                try {
//                    int totalKenRewards = 200;
//                    int totalWolRewards = 100;
//
//                    WolRewardCheckStockReq wolRewardCheckStockReq = new WolRewardCheckStockReq();
//                    ArrayList<WolAsset> assets = new ArrayList<>();
//                    WolAsset e = new WolAsset();
//                    e.setAsset_id("test_id_of_game");
//                    e.setStatus("in_game");
//                    e.setCategory("hero");
//                    e.setType("dark_knight");
//                    e.setAclass("b_rank");
//                    e.setGame(OffChainServices.GAME_MU);
//                    WolAsset b = new WolAsset();
//                    b.setAsset_id("test_id_of_game");
//                    b.setStatus("in_game");
//                    b.setCategory("hero");
//                    b.setType("dark_knight");
//                    b.setAclass("a_rank");
//                    b.setGame(OffChainServices.GAME_MU);
//                    assets.add(e);
//                    assets.add(b);
//                    wolRewardCheckStockReq.setAssets(assets);
//                    wolRewardCheckStockReq.setKen(totalKenRewards);
//                    wolRewardCheckStockReq.setWol(totalWolRewards);
//                    wolRewardCheckStockReq.setReward_id("reward_id_of_game_server_" + System.currentTimeMillis());
//
//
//                    WolRewardCheckStockRes wolRewardCheckStockRes = offChainServices.checkStockReward(wolRewardCheckStockReq);
//                    List<WolRewardCheckStockRes> wolRewardCheckStockRess = new ArrayList<>();
//                    wolRewardCheckStockRess.add(wolRewardCheckStockRes);
//
//                    WolRewardCompleteReq wolRewardCompleteReq = new WolRewardCompleteReq();
//                    wolRewardCompleteReq.setReward_id(wolRewardCheckStockRes.getReward_id());
//
//                    WolPlayerRes x000001 = new WolPlayerRes();
//                    x000001.setKen(totalKenRewards);
//                    x000001.setWol(totalWolRewards);
//                    x000001.setAddress("x000001");
//
//                    ArrayList<WolRewardPlayer> players = new ArrayList<>();
//                    WolRewardPlayer rewardPlayer = new WolRewardPlayer();
//                    rewardPlayer.setPlayer(x000001);
//                    rewardPlayer.setAssets(wolRewardCheckStockRes.getAssets());
//                    players.add(rewardPlayer);
//
//                    wolRewardCompleteReq.setPlayers(players);
//                    WolRewardCompleteRes wolAssetCompletedRes1 = offChainServices.rewardCompleted(wolRewardCompleteReq);
//                    WolRewardCompleteRes wolAssetCompletedRes = wolAssetCompletedRes1;
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //upgrade flow
//                try {
//                    WolPlayerRes x000001 = offChainServices.getBalance("x000001");
//                    WolAssetUpgradeReq wolAssetUpgradeReq = new WolAssetUpgradeReq();
//                    ArrayList<WolAsset> assetsIn = new ArrayList<>();
//                    ArrayList<WolAsset> assetsOut = new ArrayList<>();
//                    WolAsset e = new WolAsset();
//                    e.setAsset_id("test_id_of_game");
//                    e.setStatus("in_game");
//                    e.setCategory("hero");
//                    e.setType("dark_knight");
//                    e.setAclass("b_rank");
//                    e.setGame(OffChainServices.GAME_MU);
//                    WolAsset b = new WolAsset();
//                    b.setAsset_id("test_id_of_game");
//                    b.setStatus("in_game");
//                    b.setCategory("hero");
//                    b.setType("dark_knight");
//                    b.setAclass("a_rank");
//                    b.setGame(OffChainServices.GAME_MU);
//                    assetsIn.add(e);
//                    assetsOut.add(b);
//                    wolAssetUpgradeReq.setInputs(assetsIn);
//                    wolAssetUpgradeReq.setOutputs(assetsOut);
//                    wolAssetUpgradeReq.setPlayer(x000001);
//
//
//                    WolAssetUpgradeRes wolAssetUpgradeRes = offChainServices.assetUpgrade(wolAssetUpgradeReq);
//
//                    WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
//                    wolAssetCompletedReq.setAssets(wolAssetUpgradeRes.getAssets());
//                    wolAssetCompletedReq.setPlayer(wolAssetUpgradeRes.getPlayer());
//
//                    WolAssetCompletedRes wolAssetCompletedRes1 = offChainServices.assetCompleted(wolAssetCompletedReq);
//                    WolAssetCompletedRes wolAssetCompletedRes = wolAssetCompletedRes1;
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                customeGift("taolao", "add:ai:1000!12/2#1010!3/1|");

//                Utils.parseWalletAddress("0xbBE34AD3BCF74c08de4181E4FD23804fb851a18A");

                String walletAddress = "0xbbe34ad3bcf74c08de4181e4fd23804fb851a18a";
//                try {
//                    WolPlayerRes balance = OffChainServices.getInstance().getBalance(walletAddress);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
                String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiMHgzNWJiZjMxZjI3ODdiMDNiNDJhZGU2Yzk5YWM0ODU5MjkwMjQxMDdhIiwibm9uY2UiOjMxMDc0NTIsInN0YXR1cyI6ImFjdGl2ZSIsImlhdCI6MTY0ODAwODQwOSwiZXhwIjoxNjQ4MDEyMDA5fQ.UtGS76P6C4qIdM5bDBUSXH-ONnrC5wHgQgAGaGAkDMA";
//                try {
//                    DecodedJWT decodedJWT = AuthHelper.verifyToken(token);
//                    Claim address = decodedJWT.getClaim("address");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

//                JSONObject verifyToken = null;
//                try {
//                    verifyToken = OffChainServices.getInstance().verifyToken(token);
//                    if (verifyToken.containsKey("status")&&verifyToken.getString("status").equals("active")) {
//                        String deviceId = verifyToken.getString("address");}
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                //Test sum Hero
//                AtomicBoolean success = new AtomicBoolean(false);
//                Player player = playerManager.getPlayer("nf1#1008");
//                AtomicReference<HeroBase> heroBase = new AtomicReference<>();
//                OffChainResponseHandler checkStock = new OffChainResponseHandler() {
//                    @Override
//                    public JSONObject onOk(JSONObject jsonObject) {
//                        WolAssetCheckStockRes wolAssetCheckStockRes = new WolAssetCheckStockRes().init(jsonObject);
//                        HeroClass heroClass = new HeroClass(heroBase.get().getID(), 1);
//                        heroClass.setId(autoIncrService.genHeroId());
//                        heroClass.setPlayerId(player.getId());
//                        heroClassManager.save(heroClass);
//                        player.setEnergy(player.getEnergy() + heroBase.get().getEnegryCAP());
//                        playerManager.updateGameHero(player);
//                        NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
//                        notifySystem.notifyPlayerPointChange(player.getId(), player.buildPointInfo());
//                        success.set(true);
//                        return jsonObject;
//                    }
//
//                    @Override
//                    public JSONObject onNg(JSONObject jsonObject) {
//                        return null;
//                    }
//                };
//
//
//                ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
//                IntStream.range(1, 100).forEach(value -> {
//                    if (success.get()) {
//                        return;
//                    }
//                    Collections.shuffle(list);
//                    HeroBase newValue = list.get(0);
//                    heroBase.set(newValue);
//                    ArrayList<WolAsset> assets = new ArrayList<>();
//                    WolAsset e = new WolAsset();
//                    e.setAsset_id(String.valueOf(autoIncrService.getHeroId()));
//                    e.setStatus("in_game");
//                    e.setCategory("hero");
//                    e.setType(Utils.getOTypeHero(newValue.getHeroClass()));
//                    e.setAclass(Utils.getOClassHero(newValue.getRarity()));
//                    e.setGame(OffChainServices.getInstance().getGAME_KEY());
//                    assets.add(e);
//                    OffChainServices.getInstance().assetFlow(player.getWalletAddress(), assets, checkStock);
//                });
                //Test sum Hero


                //               Test reward
//                String idx = "100";
//                Stage stage = StageConfig.getInstance().getStage(idx);
//                IGenRewards genRewards = () -> {
//                    String rewards = RandomRangeUtil.randomDroprate(stage.getRandomReward(), stage.getRandomRate(), 1);
//                    String dailyFirstTimeReward = stage.getDailyFirstTimeReward();
//                    if (campaignManager.isDailyFirstTime(idx) && !Utils.isNullOrEmpty(dailyFirstTimeReward)) {
//                        rewards += "#" + dailyFirstTimeReward;
//                    }
//                    return rewards;
//                };
//
//                IApplyRewards iApplyRewards = new IApplyRewards() {
//                    @Override
//                    public void applyRewards(String rewards) {
////                        List<HeroItem> addItems = heroItemManager.addItems(user, rewards);
////                        heroItemManager.notifyAssetChange(user, addItems);
////                        ItemConfig.getInstance().buildUpdateRewardsReceipt(params, addItems);
////                        ItemConfig.getInstance().buildRewardsReceipt(params, rewards);
//                    }
//                };
//                wolAssetRewardManager.sendRewardRequest("nf1#1008", genRewards, iApplyRewards);
//               Test reward

//                ArrayList<WolAsset> assetIn = new ArrayList<>();
//                ArrayList<WolAsset> assetOut = new ArrayList<>();
//
//                OffChainServices.getInstance().buildHeroAssets(Collections.singletonList(HeroConfig.getInstance().getHeroBase("100")), assetOut);
//                List<HeroBase> heroInput = Arrays.asList(HeroConfig.getInstance().getHeroBase("100"), HeroConfig.getInstance().getHeroBase("100"));
//                OffChainServices.getInstance().buildHeroAssets(heroInput, assetIn);
//                AtomicInteger wol = new AtomicInteger();
//                AtomicInteger ken = new AtomicInteger();
//                OffChainServices.getInstance().buildItemAsset(Collections.singletonList(new HeroEquipment(ItemConfig.getInstance().getItem("1000"))), assetIn, wol, ken);
//                wolFlowManager.sendUpgradeRequest("nf1#1008", assetIn, assetOut, success -> {
//                    if (success) {
//
//                    } else {
//
//                    }
//                });

//                OffChainServices.getInstance().updateBalanceFlow(walletAddress, 1, 1, new OffChainResponseHandler() {
//                    @Override
//                    public JSONObject onOk(JSONObject jsonObject) {
//                        return jsonObject;
//                    }
//
//                    @Override
//                    public JSONObject onNg(JSONObject jsonObject) {
//                        return jsonObject;
//                    }
//                });

//                try {
//
//                    OffChainServices.getInstance().getBalance(walletAddress);
//                    OffChainServices.getInstance().getExchangeRate();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                testSumHeroNew();

                // test upgrade equip
//                EquipLevelBase equipLevelBase = ItemConfig.getInstance().getEquipLevel(3);
//                String cost1 = equipLevelBase.getCost();
//
//                String gameHeroId = "nf1#1008";
//                Player player = playerManager.getPlayer(gameHeroId);
//                OffChainServices.getInstance().updateBalanceFlow(player.getWalletAddress(), cost1, new OffChainResponseHandler() {
//                    @Override
//                    public JSONObject onOk(JSONObject jsonObject) {
//                        try {
//                            Map<String, Integer> cost = new ConcurrentHashMap<>();
//                            ItemConfig.getInstance().convertToMap(cost, cost1);
//                            Collection<HeroItem> updateItems = heroItemManager.useItemWithIndex(gameHeroId, cost);
//                            heroItemManager.save(updateItems);
//                        } catch (UseItemException e) {
//                            return jsonObject;
//                        }
//                        if (RandomRangeUtil.isSuccessPerPercent(equipLevelBase.getSuccessRatePercent(), 100)) {
//                        } else {
//                        }
//                        return jsonObject;
//                    }
//
//                    @Override
//                    public JSONObject onNg(JSONObject jsonObject) {
//                        IQAntObject createErrorMsg = MessageFactory.createErrorMsg("cmd_test", 1, GameErrorCode.LACK_OF_INFOMATION, jsonObject.toString());
//                        QAntTracer.debug(ZClientRequestHandler.class, String.join(",", jsonObject.toString()));
//                        return jsonObject;
//                    }
//                });
// test upgrade equip
//Test open Egg
//                String itemIdx = "9911";
//                ItemBase itemBase = ItemConfig.getInstance().getItem(itemIdx);
//                if (itemBase == null) {
////                    responseError(user, NOT_EXIST_ITEM);
//                    return;
//                }
//
//                Map<Long, Integer> useMap = new ConcurrentHashMap<>();
//                Collection<HeroItem> useItems = new ArrayList<>();
//
//                String name = "nf1#1002";
//                Collection<HeroItem> items = heroItemManager.getItemsByIndex(name, itemIdx);
//
//                QAntUser user=getUserByName(name);
//                user=new QAntUser(name,new EmbeddedChannel());
//                IQAntObject params=new QAntObject();
//                try {
//                    int total = 1;
//                    for (HeroItem heroItem : items) {
//                        if (total > 0) {
//                            heroItem.setNo(Math.min(total, heroItem.getNo()));
//                            total -= heroItem.getNo();
//                            useItems.add(heroItem);
//                        }
//                    }
//                    if (total > 0) {
//                        return;
//                    }
//                    useItems.forEach(heroItem -> useMap.put(heroItem.getId(), heroItem.getNo()));
//                    Collection<HeroItem> updateItems = heroItemManager.openEgg(user, useMap, params);
//                    heroItemManager.save(updateItems);
//                    heroItemManager.notifyAssetChange(user, updateItems);
//                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, updateItems);
//                } catch (UseItemException e) {
////                    responseError(user, GameErrorCode.NOT_EXIST_ITEM);
//                    return;
//                }
//Test open Egg


//            heroItemManager.notifyAssetChange(new QAntUser("nf1#1002",new EmbeddedChannel()));


                final String name = "nf1#1018";
//                Player player = playerManager.getPlayer(name);
//
//                LevelBase levelBase = HeroConfig.getInstance().getLevelUp(2);
//                String upgradeCost = levelBase.getUpgradeCost();
//
//                OffChainServices.getInstance().updateBalanceFlow(player.getWalletAddress(), upgradeCost, new OffChainResponseHandler() {
//                    @Override
//                    public JSONObject onOk(JSONObject jsonObject) {
//                        try {
//                            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(name, upgradeCost);
//                        } catch (UseItemException e) {
//                            return jsonObject;
//                        }
//                        return jsonObject;
//                    }
//
//                    @Override
//                    public JSONObject onNg(JSONObject jsonObject) {
//                        return jsonObject;
//                    }
//                });

//                Player player = playerManager.getPlayer(name);
//                syncOffchainBalance(player);
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

//        int no = 324;
//        if (no >= 100) {
//            int n=no % 100;
//            int nPlus=no / 100;
//        }

    }

//    private void testSumHeroNew() {
//        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
//
//        String name = "nf1#1008";
//        Player player = getPlayer(name);
//
//        IGenReward iGenReward = new IGenReward() {
//            @Override
//            public String genRewards() {
//                Collections.shuffle(list);
//                HeroBase newValue = list.get(0);
//                return newValue.getID()+"/1";
//            }
//
//            @Override
//            public List<RewardBase> genRewardsBase() {
//                return null;
//            }
//        };
//        IApplyAssets iApplyAssets = (rewards, wolAssetCompletedRes) -> {
//            HeroBase heroBase = HeroConfig.getInstance().getHeroBase(rewards);
//            HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
//            heroClass.setId(autoIncrService.genHeroId());
//            heroClass.setPlayerId(name);
//            OffChainServices.getInstance().applyOfcToHero(heroClass,wolAssetCompletedRes);
//            heroClassManager.save(heroClass);
//            player.setEnergy(player.getEnergy() + heroBase.getEnegryCAP());
//            NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
//            notifySystem.notifyPlayerPointChange(name, player.buildPointInfo());
//        };
//        wolFlowManager.sendAssetRequest(name, iGenReward, iApplyAssets, 100);
//    }

//    private void sendAssetRequest(String playerAddress) {
//        OffChainServices offChainServices = ExtApplication.getBean(OffChainServices.class);
//        ArrayList<WolAsset> assets = new ArrayList<>();
//        WolAsset e = new WolAsset();
//        e.setAsset_id("test_id_of_game");
//        e.setStatus("in_game");
//        e.setCategory("hero");
//        e.setType("dark_knight");
//        e.setAclass("b_rank");
//        e.setGame(offChainServices.getGAME_KEY());
//        assets.add(e);
//        OffChainResponseHandler checkStock = new OffChainResponseHandler() {
//            @Override
//            public JSONObject onOk(JSONObject jsonObject) {
//                WolAssetCheckStockRes wolAssetCheckStockRes = new WolAssetCheckStockRes().init(jsonObject);
//                applyPlayerAsset(wolAssetCheckStockRes);
//                return jsonObject;
//            }
//
//            @Override
//            public JSONObject onNg(JSONObject jsonObject) {
//                sendAssetRequest(playerAddress);
//                return null;
//            }
//        };
//        offChainServices.assetFlow(playerAddress, assets, checkStock);
//    }

//    private void sendRewardRequest() {
//        OffChainServices offChainServices = ExtApplication.getBean(OffChainServices.class);
//        ArrayList<WolAsset> assets = new ArrayList<>();
//        WolAsset e = new WolAsset();
//        e.setAsset_id("test_id_of_game");
//        e.setStatus("in_game");
//        e.setCategory("hero");
//        e.setType("dark_knight");
//        e.setAclass("b_rank");
//        e.setGame(offChainServices.getGAME_KEY());
//        WolAsset b = new WolAsset();
//        b.setAsset_id("test_id_of_game");
//        b.setStatus("in_game");
//        b.setCategory("hero");
//        b.setType("dark_knight");
//        b.setAclass("a_rank");
//        b.setGame(offChainServices.getGAME_KEY());
//        assets.add(e);
//        assets.add(b);
//        List<WolRewardPlayer> wolRewardPlayers = new ArrayList<>();
//        WolRewardPlayer rewardPlayer = new WolRewardPlayer();
//        WolPlayerRes player = new WolPlayerRes();
//        player.setWol(500);
//        player.setKen(650);
//        player.setAddress("x000001");
//        rewardPlayer.setPlayer(player);
//        rewardPlayer.setAssets(assets);
//        wolRewardPlayers.add(rewardPlayer);
//
//        OffChainResponseHandler checkStock = new OffChainResponseHandler() {
//            @Override
//            public JSONObject onOk(JSONObject jsonObject) {
//                WolRewardCheckStockRes wolAssetCheckStockRes = new WolRewardCheckStockRes().init(jsonObject);
//                offChainServices.saveRewardCompletedRequest(wolAssetCheckStockRes, wolRewardPlayers);
//                offChainServices.rewardFlowComplete(wolAssetCheckStockRes.getReward_id());
//                applyRewardCompleteFlow(wolRewardPlayers);
//                return jsonObject;
//            }
//
//            @Override
//            public JSONObject onNg(JSONObject jsonObject) {
//                sendRewardRequest();
//                return null;
//            }
//        };
//        offChainServices.rewardFlowCheckStock(wolRewardPlayers, checkStock);
//    }

    private void applyRewardCompleteFlow(List<WolRewardPlayer> wolRewardPlayers) {

    }

    private void applyPlayerAsset(WolAssetCheckStockRes jsonObject) {

    }


    private void testDailyEvent() {
        DailyEventManager dailyEventManager = ExtApplication.getBean(DailyEventManager.class);
        dailyEventManager.getDailyEvents("nf1#1001");
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

    public Player getOrCreatePlayerByDevice(String deviceId, String zoneName) {
        Player player = playerRepo.findPlayerByDeviceIdAndZoneName(deviceId, zoneName);

        if (player == null) {
            long gameId = autoIncrService.genAccountId();
            String playerId = zoneName + "#" + gameId;
            String fullname = "Guest#" + gameId;

            player = new Player(playerId, gameId, fullname);
            player.setWalletAddress(Utils.parseWalletAddress(deviceId));
            player.setDeviceId(deviceId);
            player.setZoneName(zoneName);
            updateGameHero(player);
        }
        return player;
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
        syncOffchainBalance(player);
        return player;
    }

    public void customeGift(String gameHeroId, String giftCode) {
        String[] addItemArr = StringUtils.substringsBetween(giftCode, "ai:", "|");
        if (addItemArr != null && addItemArr.length > 0) {
            heroItemManager.addItems(gameHeroId, addItemArr[0].trim());
        }
        String[] addHeroArr = StringUtils.substringsBetween(giftCode, "ah:", "|");
        if (addHeroArr != null && addHeroArr.length > 0) {
            Arrays.stream(addHeroArr[0].trim().split("#")).forEach(s -> {
                String[] split = s.split("/");
                HeroBase heroBase = HeroConfig.getInstance().getHeroBase(split[0]);
                HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
                heroClass.setId(autoIncrService.genHeroId());
                if (split.length > 1) {
                    int rank = Math.min(Integer.parseInt(split[1]), heroBase.getMaxRank());
                    heroClass.setRank(rank);
                }
                heroClass.setPlayerId(gameHeroId);
                heroClassManager.save(heroClass);
            });
        }
    }

    public void updateOffchainBalance(Player player) {
        try {
            WolPlayerRes balance = offChainServices.getInfo(player.getWalletAddress());
            List<HeroItem> currencyItem = heroItemManager.getCurrencyItem(player.getId());
            currencyItem.forEach(heroItem -> {
                switch (heroItem.getIndex()) {
                    case KEN:
                        heroItem.setNo(balance.getKen());
                        break;
                    case WOL:
                        heroItem.setNo(balance.getWol());
                        break;
                }
            });
            heroItemManager.save(currencyItem);
        } catch (Exception e) {
            QAntTracer.warn(PlayerManager.class, "updateOffchainAssets", "PLayerId :" + player.getId() + "/address : " + player.getWalletAddress());
        }
    }

    public void syncOffchainBalance(Player player) {
        try {

            WolPlayerRes balance = offChainServices.getInfo(player.getWalletAddress());
            List<HeroItem> currencyItem = heroItemManager.getCurrencyItem(player.getId());
            currencyItem.forEach(heroItem -> {
                switch (heroItem.getIndex()) {
                    case KEN:
                        heroItem.setNo(balance.getKen());
                        break;
                    case WOL:
                        heroItem.setNo(balance.getWol());
                        break;
                }
            });
            heroItemManager.save(currencyItem);
            heroItemManager.offchainSync(player.getId(), balance.getAssets().stream().filter(wolAsset -> !wolAsset.getCategory().equals("hero")).collect(Collectors.toList()));
            heroClassManager.offchainSync(player.getId(), balance.getAssets().stream().filter(wolAsset -> wolAsset.getCategory().equals("hero")).collect(Collectors.toList()));

        } catch (Exception e) {
            QAntTracer.warn(PlayerManager.class, "updateOffchainAssets", "PLayerId :" + player.getId() + "/address : " + player.getWalletAddress());
        }
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
        Player player = getPlayer(user.getName());
        IGenReward iGenReward = new IGenReward() {
            @Override
            public String genRewards() {
                Collections.shuffle(list);
                HeroBase newValue = list.get(0);
                return newValue.getID();
            }

            @Override
            public List<RewardBase> genRewardsBase() {
                return null;
            }
        };
        List<HeroClass> heroes = new ArrayList<>();
        IApplyAssets iApplyAssets = (rewards, wolAssetCompletedRes) -> {
            HeroBase heroBase = HeroConfig.getInstance().getHeroBase(rewards);
            HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
            heroClass.setId(autoIncrService.genHeroId());
            heroClass.setPlayerId(user.getName());
            OffChainServices.getInstance().applyOfcToHero(heroClass, wolAssetCompletedRes);
            heroes.add(heroClass);
            player.setEnergy(player.getEnergy() + heroBase.getEnegryCAP());
        };
        list.stream().limit(5).forEach(heroBase -> {
            wolFlowManager.sendAssetRequest(user.getName(), iGenReward, iApplyAssets, 50);
        });
        heroClassManager.save(heroes);
        updateGameHero(player);
        updateOffchainBalance(player);
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
        Collection<HeroItem> collection = new ArrayList<>();
        ItemConfig.getInstance().getItems().forEach(itemBase -> {
            HeroItem heroItem = null;
            switch (itemBase.getType()) {
                case ITEM_TICKET://material
                case ITEM_MATERIAL://material
                    heroItem = new HeroConsumeItem(itemBase);
                    switch (itemBase.getId()) {
                        case ItemRequestHandler.BLESS:
                            heroItem.setNo(100);
                            break;
                        case ItemRequestHandler.SOUL:
                            heroItem.setNo(100);
                            break;
                        default:
                            heroItem.setNo(50);
                            break;
                    }
                    collection.add(heroItem);
                    break;
                case ITEM_CURRENCY://material
                    heroItem = new HeroConsumeItem(itemBase);
                    heroItem.setNo(1);
                    collection.add(heroItem);
                    break;
                case ITEM_POINT://material
                    switch (itemBase.getId()) {
                        case ENERGY:
                            player.setEnergy(Math.min(player.getEnergyMax(), player.getEnergy() + 100));
                            break;
                        case TROPHY:
                            player.setTrophy(player.getTrophy() + 100);
                            break;
                    }
                    break;
                default:
                    break;
            }

        });
        List<HeroItem> addItems = heroItemManager.addItems(user, collection);
    }

    public void buildItemDefaultTest(QAntUser user, Player player) {
        //TODO for Test
        String gameHeroId = player.getId();
        Collection<HeroItem> collection = new ArrayList<>();
        ItemConfig.getInstance().getEquipMap().values().forEach(itemBase -> {
            for (int i = 0; i < 2; i++) {
                HeroEquipment heroEquipment1 = new HeroEquipment(itemBase);
                heroEquipment1.setNo(1);
                collection.add(heroEquipment1);
                HeroEquipment heroEquipment2 = new HeroEquipment(itemBase);
                heroEquipment2.setNo(1);
                heroEquipment2.setRank(2);
                collection.add(heroEquipment2);
            }
        });
        List<HeroItem> addItems = heroItemManager.addItems(user, collection);
        QAntTracer.debug(this.
                getClass(), "buildItemDefaultTest: " + gameHeroId);

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
        dailyEventManager.resetDailyEvents(gameHeroId);
        questSystem.notifyObservers(JoinTask.init(player.getId(), "login"));
        heroItemManager.resetItems(player);
        campaignManager.resetDaily(gameHeroId);
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

    private void resetDailyFunc(String id) {
        questSystem.resetDailyQuest(id);
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

    public Player useEnergy(String name, int no) throws UseItemException {
        Player player = getPlayer(name);
        if (!player.useEnergy(no)) {
            throw new UseItemException();
        }
        updateGameHero(player);
        return player;
    }

    public void updateOffchainAssets(String playerId, UpdateAssetRequest updateAssetRequest) {
        updateOffchainAssets(updateAssetRequest.getStatus(), playerId, updateAssetRequest.getOfcId(), getAssetId(updateAssetRequest));
    }

    public void updateOffchainAssets(String playerId, WolAsset updateAssetRequest) {
        updateOffchainAssets(updateAssetRequest.getStatus(), playerId, updateAssetRequest.getOfcId(), getAssetId(updateAssetRequest));
    }

    private void updateOffchainAssets(String status, String playerId, String ofcId, String assetId) {
        switch (status) {
            case IN_GAME:
            case REWARD:
                GameAssetDTO allByAssetIdIn = assetMappingManager.findByAssetId(assetId);
                if (allByAssetIdIn.getCategory().equals("MU-Hero")) {
                    HeroClass heroClass = new HeroClass(assetId, 1);
                    heroClass.setId(autoIncrService.genHeroId());
                    heroClass.setPlayerId(playerId);
                    heroClass.setOfcId(ofcId);
                    heroClassManager.save(heroClass);
                } else {
                    String rewards = assetId + "/1";
                    List<HeroItem> heroItems = heroItemManager.addItems(playerId, rewards);
                    heroItems.stream().limit(1).forEach(heroItem -> heroItem.setOfcId(ofcId));
                    heroItemManager.save(heroItems);
                }
                break;
            case NFT:
                break;
        }
    }

    private String getAssetId(UpdateAssetRequest updateAssetRequest) {
        String asset_id = updateAssetRequest.getAsset_id();
        String category = updateAssetRequest.getCategory();
        String type = updateAssetRequest.getType();
        return getAssetId(asset_id, category, type);
    }

    private String getAssetId(WolAsset updateAssetRequest) {
        String asset_id = updateAssetRequest.getAsset_id();
        String category = updateAssetRequest.getCategory();
        String type = updateAssetRequest.getType();
        return getAssetId(asset_id, category, type);
    }

    private String getAssetId(String asset_id, String category, String type) {
        if (Utils.isNullOrEmpty(asset_id)) {
            if (category.equals("box")) {
                switch (type) {
                    case "egg":
                        asset_id = EGG;
                        break;
                    case "starter":
                        asset_id = STARTER;
                        break;
                }
            }
        }
        return asset_id;
    }
}
