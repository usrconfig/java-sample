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
import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.services.AutoIncrementService;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private AutoIncrementService autoIncrementService;

    public ItemRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        autoIncrementService = ExtApplication.getBean(AutoIncrementService.class);
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

        Collection<HeroItem> items = heroItemManager.getItemsByIndex(user.getName(), itemIdx);

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

            Collection<HeroItem> collection = heroItemManager.useItemsWithIds(user, useMap);
            List<RewardBase> refund = new ArrayList<>();
            collection.forEach(heroItem -> heroItemManager.openItem(heroItem.getIndex(), useMap.get(heroItem.getId()), refund));

            Map<String, Integer> refundFinal = new ConcurrentHashMap<>();
            List<HeroClass> heroes = new ArrayList<>();
            refund.forEach(sRewardBase -> {
                if (sRewardBase.getType().equals("hero")) {
                    HeroClass heroClass = new HeroClass(sRewardBase.getID(), sRewardBase.getLevel());
                    heroClass.setRank(sRewardBase.getRank());
                    heroClass.setPlayerId("nf1#1001");
                    heroClass.setId(autoIncrementService.genHeroId());
                    heroes.add(heroClass);
                } else {
                    refundFinal.put(sRewardBase.getID(), sRewardBase.getCount());
                }
            });
            if (heroes.size() > 0) {
                heroClassManager.save(heroes);
                QAntArray updateArray = QAntArray.newInstance();
                heroes.forEach(hero -> updateArray.addQAntObject(hero.buildInfo()));
                params.putQAntArray("heroes", updateArray);
            }

            Player player = playerManager.getPlayer(user.getName());

            Collection<HeroItem> heroItems = ItemConfig.getInstance().convertToHeroItem(refundFinal);
            heroItems.stream().filter(heroItem -> !heroItem.getType().equals("point")).forEach(heroItem -> {
                switch (heroItem.getIndex()) {
                    case "9902":
                        player.setEnergy(Math.min(player.getEnergyMax(), player.getEnergy() + heroItem.getNo()));
                        break;
                    case "9904":
                        player.setTrophy(player.getTrophy() + heroItem.getNo());
                        break;
                }
            });
            Collection<HeroItem> items1 = heroItems.stream().filter(heroItem -> !heroItem.getType().equals("point")).collect(Collectors.toList());


            ItemConfig.getInstance().buildRewardsReceipt(params, refundFinal.keySet().stream().map(heroItem -> heroItem + "/" + refundFinal.get(heroItem)).collect(Collectors.joining("#")));
            Collection<HeroItem> updateItems = heroItemManager.addItems(user.getName(), items1);
            updateItems.addAll(collection);
            playerManager.updateGameHero(player);
            params.putQAntObject("player",player.buildPointInfo());
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
        String itemIdxPiece = "9910";
        ItemBase itemBase = ItemConfig.getInstance().getItem(itemIdx);
        if (itemBase == null) {
            responseError(user, NOT_EXIST_ITEM);
            return;
        }

        Map<Long, Integer> useMap = new ConcurrentHashMap<>();
        Collection<HeroItem> useItems = new ArrayList<>();

        Collection<HeroItem> items = heroItemManager.getItemsByIndex(user.getName(), itemIdx);

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
                try {
                    useItems.clear();
                    Collection<HeroItem> itemsPiece = heroItemManager.getItemsByIndex(user.getName(), itemIdxPiece);
                    int totalPiece = 100;
                    for (HeroItem heroItem : itemsPiece) {
                        if (totalPiece > 0) {
                            heroItem.setNo(Math.min(totalPiece, heroItem.getNo()));
                            totalPiece -= heroItem.getNo();
                            useItems.add(heroItem);
                        }
                    }
                    if (totalPiece > 0) {
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
