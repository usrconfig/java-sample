package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.hero.LevelBase;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.services.AutoIncrementService;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    private HeroItemManager heroItemManager;
    private HeroClassManager heroClassManager;
    private AutoIncrementService autoIncrService;

    public HeroRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        autoIncrService = ExtApplication.getBean(AutoIncrementService.class);
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
        }
    }

    private void levelUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        int levelMax = HeroConfig.getInstance().getMaxLevel(heroWithId.getCharIndex(), heroWithId.getRank());
        if (heroWithId.getLevel() >= levelMax) {
            responseError(user, GameErrorCode.LEVEL_MAX_NEED_RANK_UP);
            return;
        }

        LevelBase levelBase = HeroConfig.getInstance().getRankUp(heroWithId.getLevel() + 1);

        try {
            String upgradeCost = levelBase.getUpgradeCost();
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user, upgradeCost);
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

    private void rankUpHero(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        HeroClass heroWithId = heroClassManager.getHeroWithId(user.getName(), id);
        if (heroWithId == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        int maxRank = HeroConfig.getInstance().getHeroBase(heroWithId.getCharIndex()).getMaxRank();
        if (heroWithId.getRank() >= maxRank) {
            responseError(user, GameErrorCode.RANK_IS_MAX);
            return;
        }

        LevelBase levelBase = HeroConfig.getInstance().getRankUp(heroWithId.getRank() + 1);

        try {
            Collection<HeroItem> heroItems = heroItemManager.useItemWithIndex(user, levelBase.getUpgradeCost());
            heroItemManager.notifyAssetChange(user, heroItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, heroItems);
            heroItemManager.save(heroItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }

        heroWithId.rankUp();
        heroClassManager.save(heroWithId);
        params.putQAntObject("hero", heroWithId.buildInfo());
        send(params, user);
    }

    private void summonHero(QAntUser user, IQAntObject params) {
        ArrayList<HeroBase> list = new ArrayList<>(HeroConfig.getInstance().getHeroes());
        Collections.shuffle(list);
        HeroBase heroBase = list.get(0);
        HeroClass heroClass = new HeroClass(heroBase.getID(), 1);
        heroClass.setId(autoIncrService.genHeroId());
        heroClass.setPlayerId(user.getName());
        heroClassManager.save(heroClass);
        params.putQAntObject("hero", heroClass.buildInfo());
        send(params, user);

    }

    private void getPageHero(QAntUser user, IQAntObject params) {
        Integer page = params.getInt(KEYI_PAGE);
        if (page == null)
            page = 1;
        Page<HeroClass> heroPage = heroClassManager.getHeroPage(user.getName(), page);
        params.putInt(KEYI_MAX_PAGE, heroPage.getTotalPages());
        IQAntArray arr = QAntArray.newInstance();
        heroPage.getContent().forEach(item -> arr.addQAntObject(item.buildInfo()));
        params.putQAntArray(KEYQA_HEROES, arr);
        send(params, user);
    }


    @Override
    protected String getHandlerCmd() {
        return CMD_HERO;
    }

}
