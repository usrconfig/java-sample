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
import com.seagame.ext.entities.item.*;
import com.seagame.ext.exception.GameErrorCode;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.offchain.IApplyRewards;
import com.seagame.ext.offchain.IApplyUseItemRewards;
import com.seagame.ext.offchain.IGenReward;
import com.seagame.ext.offchain.entities.WolAssetCompletedRes;
import com.seagame.ext.offchain.services.OffChainResponseHandler;
import com.seagame.ext.offchain.services.OffChainServices;
import com.seagame.ext.offchain.services.WolFlowManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.services.NotifySystem;
import com.seagame.ext.util.RandomRangeUtil;
import net.sf.json.JSONObject;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private HeroItemManager heroItemManager;
    private PlayerManager playerManager;
    private HeroClassManager heroClassManager;
    private AutoIncrementService autoIncrementService;
    private WolFlowManager wolFlowManager;

    public ItemRequestHandler() {
        heroItemManager = ExtApplication.getBean(HeroItemManager.class);
        playerManager = ExtApplication.getBean(PlayerManager.class);
        heroClassManager = ExtApplication.getBean(HeroClassManager.class);
        autoIncrementService = ExtApplication.getBean(AutoIncrementService.class);
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
            IGenReward genRewards = new IGenReward() {
                @Override
                public String genRewards() {
                    return null;
                }

                @Override
                public List<RewardBase> genRewardsBase() {
                    refund.clear();
                    collection.forEach(heroItem -> heroItemManager.openItem(heroItem.getIndex(), useMap.get(heroItem.getId()), refund));
                    return refund;
                }
            };
            IApplyUseItemRewards iApplyRewards = new IApplyUseItemRewards() {
                @Override
                public void applyRewards(String rewards, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject) {

                }

                @Override
                public void applyRewards(List<RewardBase> rewardBases, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject) {
                    Map<String, Integer> refundFinal = new ConcurrentHashMap<>();
                    List<HeroClass> heroes = new ArrayList<>();
                    refund.forEach(sRewardBase -> {
                        if (sRewardBase.getType().equals("hero")) {
                            HeroClass heroClass = new HeroClass(sRewardBase.getID(), sRewardBase.getLevel());
                            heroClass.setRank(sRewardBase.getRank());
                            heroClass.setPlayerId(user.getName());
                            heroClass.setId(autoIncrementService.genHeroId());
                            heroes.add(heroClass);
                        } else {
                            refundFinal.put(sRewardBase.getID(), sRewardBase.getCount());
                        }
                    });
                    if (heroes.size() > 0) {
                        OffChainServices.getInstance().applyOfcToHeroes(heroes, wolAssetCompletedRes);
                        heroClassManager.save(heroes);
                        QAntArray updateArray = QAntArray.newInstance();
                        heroes.forEach(hero -> updateArray.addQAntObject(hero.buildInfo()));
                        params.putQAntArray("heroes", updateArray);
                    }

                    Player player = playerManager.getPlayer(user.getName());

                    Collection<HeroItem> heroItems = ItemConfig.getInstance().convertToHeroItem(refundFinal);
                    heroItems.stream().filter(heroItem -> !heroItem.getType().equals("point")).forEach(heroItem -> {
                        switch (heroItem.getIndex()) {
                            case ENERGY:
                                player.setEnergy(Math.min(player.getEnergyMax(), player.getEnergy() + heroItem.getNo()));
                                break;
                            case TROPHY:
                                player.setTrophy(player.getTrophy() + heroItem.getNo());
                                break;
                        }
                    });
                    Collection<HeroItem> items1 = heroItems.stream().filter(heroItem -> !heroItem.getType().equals("point")).collect(Collectors.toList());


                    ItemConfig.getInstance().buildRewardsReceipt(params, refundFinal.keySet().stream().map(heroItem -> heroItem + "/" + refundFinal.get(heroItem)).collect(Collectors.joining("#")));
                    Collection<HeroItem> updateItems = heroItemManager.addItems(user.getName(), items1);
                    OffChainServices.getInstance().applyOfcToItem(updateItems, wolAssetCompletedRes);
                    heroItemManager.save(updateItems);
                    updateItems.addAll(collection);
                    playerManager.updateGameHero(player);
                    NotifySystem notifySystem = ExtApplication.getBean(NotifySystem.class);
                    notifySystem.notifyPlayerPointChange(user.getName(), player.buildPointInfo());
                    heroItemManager.save(updateItems);
                    heroItemManager.notifyAssetChange(user);
                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, updateItems);
                }
            };
            wolFlowManager.sendUseItemRequest(user.getName(), collection, genRewards, iApplyRewards);

        } catch (UseItemException e) {
            responseError(user, GameErrorCode.NOT_EXIST_ITEM);
            return;
        }
        send(params, user);
    }

    private void openEgg(QAntUser user, IQAntObject params) {
        String itemIdx = EGG;
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
                responseError(user, GameErrorCode.NOT_ENOUGH_CURRENCY_ITEM);
                return;
            }
            useItems.forEach(heroItem -> useMap.put(heroItem.getId(), heroItem.getNo()));
            Collection<HeroItem> updateItems = heroItemManager.openEgg(user, useMap, params);
            heroItemManager.save(updateItems);
            heroItemManager.notifyAssetChange(user);
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
        });
        params.putQAntArray(KEYQA_ITEMS, arr);
        send(params, user);
    }


    private void levelUp(QAntUser user, IQAntObject params) {
        long id = params.getLong("id");
        if (id <= 0) {
            responseError(user, GameErrorCode.LACK_OF_INFOMATION, "id");
            return;
        }
        HeroEquipment heroItem = heroItemManager.getEquipment(id, user.getName());
        if (heroItem == null) {
            responseError(user, GameErrorCode.ITEM_NOT_FOUND, String.valueOf(id));
            return;
        }

        if (heroItem.getEquipFor() > 0) {
            responseError(user, ITEM_BEING_EQUIPPED);
            return;
        }

        EquipLevelBase equipLevelBase = ItemConfig.getInstance().getEquipLevel(heroItem.getLevel() + 1);
        String cost1 = equipLevelBase.getCost();

        Player player = playerManager.getPlayer(user.getName());
        OffChainServices.getInstance().updateBalanceFlow(player.getWalletAddress(), cost1, new OffChainResponseHandler() {

            @Override
            public JSONObject onOk(JSONObject jsonObject) {
                try {
                    Map<String, Integer> cost = new ConcurrentHashMap<>();
                    ItemConfig.getInstance().convertToMap(cost, cost1);
                    Collection<HeroItem> updateItems = heroItemManager.useItemWithIndex(user.getName(), cost);
                    heroItemManager.save(updateItems);
                    heroItemManager.notifyAssetChange(user, updateItems);
                    ItemConfig.getInstance().buildUpdateRewardsReceipt(params, updateItems);
                } catch (UseItemException e) {
                    responseError(user, GameErrorCode.LACK_OF_MATIRIAL, "UseItemException");
                    return jsonObject;
                }
                if (RandomRangeUtil.isSuccessPerPercent(equipLevelBase.getSuccessRatePercent(), 100)) {
                    heroItem.levelUp();
                    params.putBool("success", true);
                } else {
                    int levelDown = equipLevelBase.getLevelDown();
                    heroItem.setLevel(heroItem.getLevel() - levelDown);
                    params.putBool("success", false);
                }
                if (heroItem.getLevel() > 0) {
                    heroItemManager.save(heroItem);
                } else {
                    heroItemManager.remove(heroItem);
                }
                params.putQAntObject("item", heroItem.buildInfo());
                send(params, user);
                return jsonObject;
            }

            @Override
            public JSONObject onNg(JSONObject jsonObject) {
                responseError(user, CHECK_OFFCHAIN_ASSET, jsonObject.toString());
                return jsonObject;
            }
        });

    }


    @Override
    protected String getHandlerCmd() {
        return CMD_ITEM;
    }

}
