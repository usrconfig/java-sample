package com.seagame.ext.controllers;

import com.creants.creants_2x.core.annotations.Instantiation;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.item.HeroEquipment;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.item.ItemBase;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.seagame.ext.exception.GameErrorCode.*;

/**
 * @author LamHM
 */
@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class ItemRequestHandler extends ZClientRequestHandler {
    private static final int PAGE_ITEM = 1;
    private static final int USE_ITEM = 2;
    private static final int LEVEL_UP = 3;
    private static final int EQUIP_UNEQUIP = 4;
    private static final int OPEN_EGG = 5;
    public static final String EGG_PIECE = "9910";
    public static final int EGG_PIECE_TO_EGE = 1000;
    public static final String EGG = "9911";

    private HeroItemManager heroItemManager;
    private PlayerManager playerManager;
    private HeroClassManager heroClassManager;

    public ItemRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
    }


    @Override
    public void handleClientRequest(QAntUser user, IQAntObject params) {
        Integer action = this.getAction(params);
        if (action == null) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        switch (action) {
            case PAGE_ITEM:
                pageItem(user, params);
                break;
            case LEVEL_UP:
                levelUp(user, params);
                break;
            case EQUIP_UNEQUIP:
                equipUnEquipItem(user, params);
                break;
            case USE_ITEM:
                useItem(user, params);
                break;
            case OPEN_EGG:
                openEgg(user, params);
                break;
        }
    }

    private void useItem(QAntUser user, IQAntObject params) {
        String itemIdx = params.getUtfString("idx");
        int no = params.getInt("no");

        ItemBase itemBase = ItemConfig.getInstance().getItem(itemIdx);
        if (itemBase == null) {
            responseError(user, NOT_EXIST_ITEM);
            return;
        }

        if (itemIdx == null || no <= 0) {
            responseError(user, LACK_OF_INFOMATION);
            return;
        }
        if (itemBase.getRewards() == null || itemBase.getRewards().size() <= 0) {
            responseError(user, NOT_ENOUGH_CURRENCY_ITEM);
            return;
        }


        Map<Long, Integer> useMap = new ConcurrentHashMap<>();
        Collection<HeroItem> useItems = new ArrayList<>();

        Player player = playerManager.getPlayer(user.getName());
        Collection<HeroItem> items = heroItemManager.getItemsByIndex(user.getName(), player.getActiveHeroId(), itemIdx);

        try {
            int total = no;
            for (HeroItem heroItem : items) {
                if (total > 0) {
                    heroItem.setNo(Math.min(total, heroItem.getNo()));
                    total -= heroItem.getNo();
                    useItems.add(heroItem);
                }
            }
            if (total > 0) {
                responseError(user, LACK_OF_INFOMATION);
                return;
            }
            useItems.forEach(heroItem -> useMap.put(heroItem.getId(), heroItem.getNo()));
            Collection<HeroItem> updateItems = heroItemManager.useItems(user, useMap, params);
            heroItemManager.save(updateItems);
            heroItemManager.notifyAssetChange(user, updateItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, updateItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_EXIST_ITEM);
            return;
        }
        send(params, user);
    }

    private void openEgg(QAntUser user, IQAntObject params) {
        String itemIdx = "9911";
        ItemBase itemBase = ItemConfig.getInstance().getItem(itemIdx);
        if (itemBase == null) {
            responseError(user, NOT_EXIST_ITEM);
            return;
        }

        Map<Long, Integer> useMap = new ConcurrentHashMap<>();
        Collection<HeroItem> useItems = new ArrayList<>();

        Player player = playerManager.getPlayer(user.getName());
        Collection<HeroItem> items = heroItemManager.getItemsByIndex(user.getName(), player.getActiveHeroId(), itemIdx);

        try {
            int total = 1;
            for (HeroItem heroItem : items) {
                if (total > 0) {
                    heroItem.setNo(Math.min(total, heroItem.getNo()));
                    total -= heroItem.getNo();
                    useItems.add(heroItem);
                }
            }
            if (total > 0) {
                responseError(user, LACK_OF_INFOMATION);
                return;
            }
            useItems.forEach(heroItem -> useMap.put(heroItem.getId(), heroItem.getNo()));
            Collection<HeroItem> updateItems = heroItemManager.openEgg(user, useMap, params);
            heroItemManager.save(updateItems);
            heroItemManager.notifyAssetChange(user, updateItems);
            ItemConfig.getInstance().buildUpdateRewardsReceipt(params, updateItems);
        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_EXIST_ITEM);
            return;
        }
        send(params, user);
    }

    private void equipUnEquipItem(QAntUser user, IQAntObject params) {
        Collection<Long> ids = params.getLongArray("ids");
        long heroId = params.getLong("heroId");
        Collection<HeroItem> items = new ArrayList<>();
        Collection<HeroItem> takeOff = heroItemManager.getTakeOnEquipments(user.getName(), heroId);
        HeroClass heroClass = heroClassManager.getHeroWithId(user.getName(), heroId);
        if (heroClass == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        QAntArray qAntArray = new QAntArray();
        takeOff.stream().filter(heroItem -> !ids.contains(heroItem.getId())).forEach(heroItem -> {
            if (heroItem instanceof HeroEquipment) heroItem.setEquipFor(-1);
            qAntArray.addQAntObject(heroItem.buildInfo());
            items.add(heroItem);
        });

        Collection<HeroItem> heroItems = heroItemManager.getItemsByIds(user.getName(), ids);
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroEquipment) heroItem.setEquipFor(heroId);
            qAntArray.addQAntObject(heroItem.buildInfo());
            items.add(heroItem);
        });
        heroItemManager.save(items);
        params.putQAntArray("items", qAntArray);
        send(params, user);
    }

    private void pageItem(QAntUser user, IQAntObject params) {
        Integer page = params.getInt(KEYI_PAGE);
        if (page == null)
            page = 1;

        Page<HeroItem> itemPage = heroItemManager.getItemList(user.getName(), page);
        params.putInt(KEYI_MAX_PAGE, itemPage.getTotalPages());

        IQAntArray arr = QAntArray.newInstance();
        itemPage.getContent().forEach(item -> {
            arr.addQAntObject(item.buildInfo());
            if (item.getIndex().equals(EGG_PIECE)) {
                if (item.getNo() >= EGG_PIECE_TO_EGE) {
                    item.setNo(Math.floorMod(item.getNo(), EGG_PIECE_TO_EGE));
                    Collection<HeroItem> itemsById = heroItemManager.getItemsById(user.getName(), EGG);
                    AtomicInteger no = new AtomicInteger(0);
                    itemsById.stream().limit(1).forEach(heroItem -> {
                        no.set(heroItem.getNo() + Math.floorDiv(item.getNo(), EGG_PIECE_TO_EGE));
                        heroItem.setNo(no.get());
                    });
                    itemsById.add(item);
                    heroItemManager.save(itemsById);
                    itemPage.getContent().stream().filter(heroItem -> heroItem.getIndex().equals(EGG)).forEach(heroItem -> heroItem.setNo(no.get()));
                }
            }
        });
        params.putQAntArray(KEYQA_ITEMS, arr);
        send(params, user);
    }


    private void levelUp(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        if (id <= 0) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        HeroEquipment heroItem = heroItemManager.getEquipment(id, user.getName());
        if (heroItem == null) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION);
            return;
        }
        heroItem.levelUp();
        params.putQAntObject("item", heroItem.buildInfo());
        send(params, user);
    }


    @Override
    protected String getHandlerCmd() {
        return CMD_ITEM;
    }

}
