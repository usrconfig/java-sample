package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.config.game.SkillConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.hero.LevelBase;
import com.seagame.ext.entities.hero.skill.Skill;
import com.seagame.ext.entities.hero.skill.SkillBase;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.offchain.IApplyAssets;
import com.seagame.ext.offchain.IGenReward;
import com.seagame.ext.offchain.entities.WolAsset;
import com.seagame.ext.offchain.services.OffChainResponseHandler;
import com.seagame.ext.offchain.services.OffChainServices;
import com.seagame.ext.offchain.services.WolFlowManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.services.NotifySystem;
import com.seagame.ext.util.RandomRangeUtil;
import net.sf.json.JSONObject;
import org.springframework.data.domain.Page;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.seagame.ext.exception.GameErrorCode.LACK_OF_INFOMATION;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class HeroRequestHandler extends ZClientRequestHandler {

    private static final int PAGE_HERO = 1;
    private static final int SUMMON_HERO = 2;
    private static final int RANK_UP_HERO = 3;
    private static final int LEVEL_UP_HERO = 4;
    private static final int SKILL_UP_HERO = 5;


    private static final String SUM_COST = "9900/10";

    private HeroItemManager heroItemManager;
    private HeroClassManager heroClassManager;
    private PlayerManager playerManager;
    private AutoIncrementService autoIncrService;
    private WolFlowManager wolFlowManager;

    public HeroRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        autoIncrService = ExtApplication.getBean(AutoIncrementService.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        wolFlowManager = ExtApplication.getBean(WolFlowManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case PAGE_HERO:
                getPageHero(user, params);
                break;
            case SUMMON_HERO:
                summonHero(user, params);
                break;
            case RANK_UP_HERO:
                rankUpHero(user, params);
                break;
            case LEVEL_UP_HERO:
                levelUpHero(user, params);
                break;
            case SKILL_UP_HERO:
                skillUpHero(user, params);
                break;
        }
    }

    private void levelUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null) {
            responseError(user, GameErrorCode.HERO_NOT_FOUND);
            return;
        }
        if (isMaxLevel(heroWithId)) {
            responseError(user, GameErrorCode.LEVEL_MAX_NEED_RANK_UP);
            return;
        }

        Player player = playerManager.getPlayer(user.getName());

        LevelBase levelBase = HeroConfig.getInstance().getLevelUp(heroWithId.getLevel() + 1);
        String upgradeCost = levelBase.getUpgradeCost();

        OffChainServices.getInstance().updateBalanceFlow(player.getWalletAddress(), upgradeCost, new OffChainResponseHandler() {
            @Override
            public JSONObject onOk(JSONObject jsonObject) {
                try {
                    Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), upgradeCost);
                    heroItemManager.notifyAssetChange(user, heroItems);
                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
                    heroItemManager.save(heroItems);
                } catch (UseItemException e) {
                    responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
                    return jsonObject;
                }

                heroWithId.levelUp(1);
                heroClassManager.save(heroWithId);
                params.putQAntObject("hero", heroWithId.buildInfo());
                send(params, user);
                return jsonObject;
            }

            @Override
            public JSONObject onNg(JSONObject jsonObject) {
                responseError(user, GameErrorCode.CHECK_OFFCHAIN_ASSET, jsonObject.toString());
                return jsonObject;
            }
        });

    }

    private boolean isMaxLevel(HeroClass heroWithId) {
        return heroWithId.getLevel() >= HeroConfig.getInstance().getMaxLevel(heroWithId.getCharIndex(), heroWithId.getRank());
    }

    private void skillUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        String skillIdx = params.getUtfString("skillIdx");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        Player player = playerManager.getPlayer(user.getName());
        if (heroWithId == null) {
            responseError(user, GameErrorCode.HERO_NOT_FOUND);
            return;
        }
        Skill upSkill = heroWithId.getSkills().stream().filter(skill -> skill.getIndex().equals(skillIdx)).findFirst().orElse(null);
        if (upSkill == null) {
            responseError(user, GameErrorCode.SKILL_NOT_FOUND);
            return;
        }
        SkillBase skillBase = SkillConfig.getInstance().getSkillMap().get(skillIdx);
        if (skillBase == null) {
            responseError(user, GameErrorCode.SKILL_NOT_FOUND);
            return;
        }
        if (upSkill.getLevel() >= skillBase.getMaxLevel()) {
            responseError(user, GameErrorCode.SKILL_MAX_LEVEL);
            return;
        }
        String upgradeCost = SkillConfig.getInstance().getUpgradeBaseMap().get(upSkill.level).stream().map(skillUpgradeBase -> skillUpgradeBase.getItemID() + "/" + skillUpgradeBase.getCount()).collect(Collectors.joining("#"));
        OffChainServices.getInstance().updateBalanceFlow(player.getWalletAddress(), upgradeCost, new OffChainResponseHandler() {
            @Override
            public JSONObject onOk(JSONObject jsonObject) {
                try {
                    Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), upgradeCost);
                    heroItemManager.notifyAssetChange(user, heroItems);
                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
                    heroItemManager.save(heroItems);
                } catch (UseItemException e) {
                    responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
                    return jsonObject;
                }

                upSkill.levelUp(1);
                heroWithId.calcFullPower();
                heroClassManager.save(heroWithId);
                params.putQAntObject("hero", heroWithId.buildInfo());
                send(params, user);
                return jsonObject;
            }

            @Override
            public JSONObject onNg(JSONObject jsonObject) {
                responseError(user, GameErrorCode.CHECK_OFFCHAIN_ASSET, jsonObject.toString());
                return jsonObject;
            }
        });

    }

    private void rankUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        long useHero = params.getLong("useHero");
        long useItem = params.containsKey("useItem") ? params.getLong("useItem") : 0;
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null) {
            responseError(user, GameErrorCode.HERO_NOT_FOUND, String.valueOf(id));
            return;
        }
        if (!isMaxLevel(heroWithId)) {
            responseError(user, GameErrorCode.HERO_MAX_LEVEL_REQUIRE, String.valueOf(id));
            return;
        }
        HeroClass heroUse = heroClassManager.getHeroWithId(user.getName(), useHero);
        if (heroUse == null) {
            responseError(user, GameErrorCode.HERO_NOT_FOUND, String.valueOf(useHero));
            return;
        }
        if (!isMaxLevel(heroUse)) {
            responseError(user, GameErrorCode.HERO_MAX_LEVEL_REQUIRE, String.valueOf(useHero));
            return;
        }

        if (heroUse.getRank() != heroWithId.getRank()) {
            responseError(user, GameErrorCode.HERO_SAME_RANK_REQUIRE);
            return;
        }


        int bonus = 0;
        HeroItem heroItem = null;
        List<HeroItem> heroItems1 = new ArrayList<>();
        if (useItem > 0) {
            heroItem = heroItemManager.getEquipment(useItem, user.getName());
            if (heroItem == null) {
                responseError(user, GameErrorCode.ITEM_NOT_FOUND, String.valueOf(useItem));
                return;
            }
            if (heroItem.getEquipFor() > 0) {
                responseError(user, GameErrorCode.ITEM_BEING_EQUIPPED, String.valueOf(useItem));
                return;
            }

            int reqItemRank = 2;
            if (heroWithId.getRank() == 2)
                reqItemRank = 3;

            if (heroItem.getRank() < reqItemRank) {
                responseError(user, GameErrorCode.ITEM_RANK_REQUIRE, String.valueOf(reqItemRank));
                return;
            }
            bonus = heroItem.getLevel() * 3;
            heroItems1.add(heroItem);
        }

        if (isMaxRank(heroWithId)) {
            responseError(user, GameErrorCode.RANK_IS_MAX);
            return;
        }
        String upRankCost = getUpRankCost(heroWithId.getRank());

        heroItems1.addAll(ItemConfig.getInstance().splitItemToHeroItem(upRankCost));

        ArrayList<WolAsset> assetIn = new ArrayList<>();
        ArrayList<WolAsset> assetOut = new ArrayList<>();

        OffChainServices.getInstance().buildHeroAssets(Collections.singletonList(heroWithId), assetOut);
        List<HeroClass> heroInput = Collections.singletonList(heroUse);
        OffChainServices.getInstance().buildHeroAssets(heroInput, assetIn);
        AtomicInteger wol = new AtomicInteger();
        AtomicInteger ken = new AtomicInteger();
        OffChainServices.getInstance().buildItemAsset(heroItems1, assetIn, wol, ken);
        int finalBonus = bonus;
        HeroItem finalHeroItem = heroItem;
        wolFlowManager.sendUpgradeRequest(user.getName(), assetIn, assetOut, (success, wolAssetCompletedRes, jsonObject) -> {
            if (success) {
                try {
                    Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), upRankCost);
                    heroItemManager.notifyAssetChange(user, heroItems);
                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
                    heroItemManager.save(heroItems);
                } catch (UseItemException e) {
                    responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
                    return;
                }
                heroClassManager.remove(heroUse);
                if (finalHeroItem != null)
                    heroItemManager.remove(finalHeroItem);
                boolean successPerPercent = RandomRangeUtil.isSuccessPerPercent(getUpRankRate(heroWithId.getRank()) + finalBonus, 100);
                if (successPerPercent) {
                    heroWithId.rankUp();
                    params.putBool("success", true);
                } else {
                    params.putBool("success", false);
                }
                OffChainServices.getInstance().applyOfcToHero(heroWithId, wolAssetCompletedRes);
                heroClassManager.save(heroWithId);
                params.putQAntObject("hero", heroWithId.buildInfo());
                send(params, user);
            } else {
                responseError(user, GameErrorCode.CHECK_OFFCHAIN_ASSET, jsonObject.toString());
            }
        }, -wol.get(), -ken.get());
    }

    private boolean isMaxRank(HeroClass heroWithId) {
        return heroWithId.getRank() >= HeroConfig.getInstance().getHeroBase(heroWithId.getCharIndex()).getMaxRank();
    }

    private String getUpRankCost(int rank) {
        switch (rank) {
            case 1:
                return WOL + "/10#" + KEN + "/1000";
            case 2:
                return WOL + "/80#" + KEN + "/8000";
            default:
                return "";

        }
    }

    private int getUpRankRate(int rank) {
        switch (rank) {
            case 1:
                return 50;
            case 2:
                return 30;
            default:
                return 0;

        }
    }

    private void summonHero(QAntUser user, IQAntObject params) {
        try {
            heroItemManager.useItemWithIndex(user.getName(), SUM_COST);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }

        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());

        Player player = playerManager.getPlayer(user.getName());

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
        IApplyAssets iApplyAssets = (rewards, wolAssetCompletedRes) -> {
            HeroBase heroBase = HeroConfig.getInstance().getHeroBase(rewards);
            HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
            heroClass.setId(autoIncrService.genHeroId());
            heroClass.setPlayerId(user.getName());
            OffChainServices.getInstance().applyOfcToHero(heroClass, wolAssetCompletedRes);
            heroClassManager.save(heroClass);
            params.putQAntObject("hero", heroClass.buildInfo());
            player.setEnergy(player.getEnergy() + heroBase.getEnegryCAP());
            playerManager.updateGameHero(player);
            playerManager.updateOffchainBalance(player);
            NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
            notifySystem.notifyPlayerPointChange(user.getName(), player.buildPointInfo());
            send(params, user);
            heroItemManager.notifyAssetChange(user);
        };
        wolFlowManager.sendAssetRequest(user.getName(), iGenReward, iApplyAssets, 100);
    }


    private void getPageHero(QAntUser user, IQAntObject params) {
        Integer page = params.getInt(KEYI_PAGE);
        if (page == null)
            page = 1;
        Page<HeroClass> heroPage = heroClassManager.getHeroPage(user.getName(), page);
        params.putInt(KEYI_MAX_PAGE, heroPage.getTotalPages());
        IQAntArray arr = QAntArray.newInstance();
        heroPage.getContent().forEach(item -> {
            heroClassManager.setHeroBaseAndEquipment(item);
            arr.addQAntObject(item.buildInfo());
        });
        params.putQAntArray(KEYQA_HEROES, arr);
        send(params, user);
    }


    @Override
    protected String getHandlerCmd() {
        return CMD_HERO;
    }

}
