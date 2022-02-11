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
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.RandomRangeUtil;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public HeroRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        autoIncrService = ExtApplication.getBean(AutoIncrementService.class);
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
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        if (isMaxLevel(heroWithId)) {
            responseError(user, GameErrorCode.LEVEL_MAX_NEED_RANK_UP);
            return;
        }

        LevelBase levelBase = HeroConfig.getInstance().getLevelUp(heroWithId.getLevel() + 1);

        try {
            String upgradeCost = levelBase.getUpgradeCost();
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), upgradeCost);
            heroItemManager.notifyAssetChange(user, heroItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }

        heroWithId.levelUp(1);
        heroClassManager.save(heroWithId);
        params.putQAntObject("hero", heroWithId.buildInfo());
        send(params, user);
    }

    private boolean isMaxLevel(HeroClass heroWithId) {
        return heroWithId.getLevel() >= HeroConfig.getInstance().getMaxLevel(heroWithId.getCharIndex(), heroWithId.getRank());
    }

    private void skillUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        String skillIdx = params.getUtfString("skillIdx");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
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

        try {
            String upgradeCost = SkillConfig.getInstance().getUpgradeBaseMap().get(upSkill.level).stream().map(skillUpgradeBase -> skillUpgradeBase.getItemID() + "/" + skillUpgradeBase.getCount()).collect(Collectors.joining("#"));
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), upgradeCost);
            heroItemManager.notifyAssetChange(user, heroItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }

        upSkill.levelUp(1);
        heroWithId.calcFullPower();
        heroClassManager.save(heroWithId);
        params.putQAntObject("hero", heroWithId.buildInfo());
        send(params, user);
    }

    private void rankUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        long useHero = params.getLong("useHero");
        long useItem = params.getLong("useItem");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null || !isMaxLevel(heroWithId)) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        HeroClass heroUse = heroClassManager.getHeroWithId(user.getName(), useHero);
        if (heroUse == null || heroUse.getRank() != heroWithId.getRank() || !isMaxLevel(heroUse)) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        HeroItem heroItem = heroItemManager.getEquipment(useItem, user.getName());
        if (heroItem == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }

        if (isMaxRank(heroWithId)) {
            responseError(user, GameErrorCode.RANK_IS_MAX);
            return;
        }

        try {
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), getUpRankCost(heroWithId.getRank()));
            heroItemManager.notifyAssetChange(user, heroItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }
        heroClassManager.remove(heroUse);
        heroItemManager.remove(heroItem);
        if (RandomRangeUtil.isSuccessPerPercent(getUpRankRate(heroWithId.getRank()), 100))
            heroWithId.rankUp();
        heroClassManager.save(heroWithId);
        params.putQAntObject("hero", heroWithId.buildInfo());
        send(params, user);
    }

    private boolean isMaxRank(HeroClass heroWithId) {
        return heroWithId.getRank() >= HeroConfig.getInstance().getHeroBase(heroWithId.getCharIndex()).getMaxRank();
    }

    private String getUpRankCost(int rank) {
        switch (rank) {
            case 1:
                return WOL + "/10#" + KEN + "/1000";
            case 2:
                return WOL + "/80##" + KEN + "/8000";
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
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user.getName(), SUM_COST);
            heroItemManager.notifyAssetChange(user, heroItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }
        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
        Collections.shuffle(list);
        HeroBase heroBase = list.get(0);
        HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
        heroClass.setId(autoIncrService.genHeroId());
        heroClass.setPlayerId(user.getName());
        heroClassManager.save(heroClass);
        params.putQAntObject("hero", heroClass.buildInfo());
        send(params, user);
        Player player = playerManager.getPlayer(user.getName());
        player.setEnergy(player.getEnergy() + heroBase.getEnegryCAP());
        playerManager.updateGameHero(player);
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
