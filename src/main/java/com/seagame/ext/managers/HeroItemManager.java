package com.seagame.ext.managers;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.controllers.ItemRequestHandler;
import com.seagame.ext.dao.HeroItemRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.item.*;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.offchain.IApplyRewards;
import com.seagame.ext.offchain.IApplyUseItemRewards;
import com.seagame.ext.offchain.IGenReward;
import com.seagame.ext.offchain.entities.WolAsset;
import com.seagame.ext.offchain.entities.WolAssetCompletedRes;
import com.seagame.ext.offchain.services.OffChainServices;
import com.seagame.ext.offchain.services.WolFlowManager;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.NetworkConstant;
import com.seagame.ext.util.RandomRangeUtil;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
@Service
public class HeroItemManager extends AbstractExtensionManager implements InitializingBean, NetworkConstant, ExtensionEvent {
    private static final int MAX_ITEM_PER_PAGE = 20;
    private static final ItemConfig itemConfig = ItemConfig.getInstance();
    @Autowired
    private HeroItemRepository heroItemRep;
    @Autowired
    private HeroClassManager heroClassManager;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private AutoIncrementService autoIncrService;
    @Autowired
    private WolFlowManager wolFlowManager;

    @Override
    public void afterPropertiesSet() {

//        ConcurrentHashMap<String, Integer> refund = new ConcurrentHashMap<>();
//        openItem("ITEM300", 10, refund);
//        Map<String, Map<String, Integer>> stringMapMap = applySkinTest(refund);
//        ItemConfig.getInstance().buildRewardsSkinReceipt(new QAntObject(), stringMapMap);
    }


    public void removeGameHeroData(String gameHeroId) {
        heroItemRep.remove(gameHeroId);
    }


    public Page<HeroItem> getItemList(String gameHeroId, int page) {
        Player player = playerManager.getPlayer(gameHeroId);
        Page<HeroItem> itemPage = heroItemRep.getItemList(gameHeroId, player.getActiveHeroId(),
                PageRequest.of(page - 1, MAX_ITEM_PER_PAGE));
        itemPage.getContent()
                .forEach(heroItem -> heroItem.initItemBase());
        return itemPage;
    }


//    public Page<HeroItem> getItemInCofferExceptMoneyType(String gameHeroId, int page) {
//        Page<HeroItem> itemPage = heroItemRep.getItemInCoffer(gameHeroId, PageRequest.of(page - 1, MAX_ITEM_PER_PAGE));
//        itemPage.getContent()
//                .forEach(heroItem -> heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex())));
//
//        IQAntArray arr = QAntArray.newInstance();
//        itemPage.getContent().forEach(item -> arr.addQAntObject(QAntObject.newFromObject(item)));
//        System.out.println(arr.getDump());
//        return itemPage;
//    }

    public List<HeroItem> getConsumableItem(String gameHeroId) {
        return heroItemRep.getConsumableItem(gameHeroId);
    }

    public List<HeroItem> getCurrencyItem(String gameHeroId) {
        return heroItemRep.getCurrencyItem(gameHeroId);
    }


    public void save(Collection<HeroItem> items) {
        update(items);
    }

    public void save(HeroItem items) {
        heroItemRep.save(items);
    }


    public void remove(HeroItem equipment) {
        heroItemRep.delete(equipment);
    }


    public void remove(Collection<HeroItem> items) {
        heroItemRep.deleteAll(items);
    }


    public List<HeroItem> getTakeOnEquipments(String player, long heroId) {
        List<HeroItem> items = heroItemRep.getEquipmentFor(player, heroId);
        QAntTracer.debug(HeroItemManager.class, player);
        QAntTracer.debug(HeroItemManager.class, items.toString());
        items.forEach(HeroItem::initItemBase);
        return items;
    }


    public HeroEquipment getEquipment(long itemId, String gameHeroId) {
        HeroEquipment equipment = heroItemRep.getEquipment(itemId, gameHeroId);
        if (equipment != null)
            equipment.initItemBase();
        return equipment;
    }


//    public List<HeroItem> putItemInToCoffer(String gameHeroId, Collection<Long> itemIds, boolean isPutIn) {
//        List<HeroItem> itemList = heroItemRep.getItemByItemId(gameHeroId, itemIds);
//        itemList.forEach(item -> {
//            item.putInCoffer(isPutIn);
//            item.setItemBase(itemConfig.getItem(item.getIndex()));
//        });
//
//        if (itemList.size() <= 0)
//            return new ArrayList<>();
//        return heroItemRep.saveAll(itemList);
//    }


    public HeroItem useItem(QAntUser user, String itemIndex, int useNo) throws UseItemException {
        if (StringUtils.isNotEmpty(itemIndex) && useNo > 0) {
            Collection<HeroItem> heroItems = this.useItemWithIndex(user.getName(), itemIndex + "/" + useNo);
            if (heroItems != null) {
                return heroItems.stream().findFirst().orElse(null);
            }
        }
        return null;
    }

    public Collection<HeroItem> useItemsWithIds(QAntUser user, Map<Long, Integer> items) throws UseItemException {
        String gameHeroId = user.getName();
        List<HeroItem> consumeAbleItems = getConsumableItems(gameHeroId, items.keySet());
        if (consumeAbleItems.size() != items.size())
            throw UseItemException.lackOfItem();

        Optional<HeroItem> lackOfItem = consumeAbleItems.stream()
                .filter(item -> item.decr(items.get(item.getId())) < 0).findFirst();
        if (lackOfItem.isPresent())
            throw UseItemException.lackOfItem();
        return consumeAbleItems;

    }

    public Collection<HeroItem> useItemWithIndex(String playerId, String itemString) throws UseItemException {
        Map<String, Integer> itemMap = itemConfig.convertToMap(itemString);
        return this.useItemWithIndex(playerId, itemMap);
    }

    public Collection<HeroItem> useItemWithIndex(String playerId, Map<String, Integer> itemMap) throws UseItemException {
        List<HeroItem> consumeAbleItems = getByIndexes(playerId, itemMap.keySet());
        itemMap.keySet().forEach(s -> {
            final int[] val = {itemMap.get(s)};
            consumeAbleItems.forEach(heroItem -> {
                if (heroItem.getIndex().equals(s)) {
                    int desc = Math.min(heroItem.getNo(), val[0]);
                    heroItem.setNo(heroItem.getNo() - desc);
                    val[0] -= desc;
                }
            });
            itemMap.put(s, val[0]);
        });
        if (itemMap.values().stream().anyMatch(integer -> integer > 0))
            throw UseItemException.lackOfItem();
        return consumeAbleItems;
    }


    private List<HeroItem> getConsumableItems(String gameHeroId, Set<Long> indexes) {
        return heroItemRep.getItemByItemId(gameHeroId, indexes);
    }

    public List<HeroItem> getByIndexes(String gameHeroId, Set<String> indexes) {
        Player player = playerManager.getPlayer(gameHeroId);
        return heroItemRep.getItemList(gameHeroId, indexes);
    }


    private void update(HeroItem heroItem) {
        if (heroItem.getNo() <= 0 && heroItem.canBeRemoved())
            heroItemRep.delete(heroItem);
        else
            heroItemRep.save(heroItem);
    }


    private void update(Collection<HeroItem> heroItems) {
        heroItemRep.deleteAll(heroItems.stream().filter(HeroItem::canBeRemoved).collect(Collectors.toList()));
        heroItemRep.saveAll(heroItems.stream().filter(HeroItem::canNotRemove).collect(Collectors.toList()));
    }

    public List<HeroItem> addItems(QAntUser user, String itemArrString) {
        return addItems(user.getName(), itemConfig.splitItemToHeroItem(itemArrString));
    }

    public List<HeroItem> addItems(String gameHeroId, String itemArrString) {
        return addItems(gameHeroId, itemConfig.splitItemToHeroItem(itemArrString));
    }

    public List<HeroItem> addItems(QAntUser user, Collection<HeroItem> items) {
        return addItems(user.getName(), items);
    }

    public List<HeroItem> addItems(String playerId, Collection<HeroItem> items) {
        Player player = playerManager.getPlayer(playerId);
        long activeHeroId = player.getActiveHeroId();
        if (activeHeroId > 0) {
            return this.addItemsForActiveHero(playerId, items, activeHeroId);
        } else {
            return this.addItemsPlayer(playerId, items);
        }
    }

    private List<HeroItem> addItemsForActiveHero(String playerId, Collection<HeroItem> items, long activeHeroId) {
        if (items == null || items.size() <= 0)
            return new ArrayList<>();
        Set<String> collect = items.stream().filter(HeroItem::isOverlap).map(HeroItem::getIndex).collect(Collectors.toSet());
        List<HeroItem> itemsUpdate = heroItemRep.getItemListHeroId(playerId,
                collect, activeHeroId);

        // cập nhật số lượng item cho overlap
        List<String> overlapIndexes = itemsUpdate.stream().map(item -> item.incr(sumOverlapItemNo(items, item)))
                .map(HeroItem::getIndex).collect(Collectors.toList());

        // tạo item mới
        itemsUpdate
                .addAll(items.stream().filter(heroItem -> !overlapIndexes.contains(heroItem.getIndex())).peek(item -> {
                    item.setId(autoIncrService.genItemId());
                    item.setPlayerId(playerId);
                    item.setHeroId(activeHeroId);
                }).collect(Collectors.toList()));

        heroItemRep.saveAll(itemsUpdate);
        //apply changed value for return to client
//        itemsUpdate.stream().filter(HeroItem::isOverlap).forEach(item -> item.setNo(sumOverlapItemNo(items, item)));
        return itemsUpdate;
    }

    private List<HeroItem> addItemsPlayer(String playerId, Collection<HeroItem> items) {
        if (items == null || items.size() <= 0)
            return new ArrayList<>();
        Set<String> collect = items.stream().filter(HeroItem::isOverlap).map(HeroItem::getIndex).collect(Collectors.toSet());
        List<HeroItem> itemsUpdate = heroItemRep.getItemList(playerId,
                collect);

        // cập nhật số lượng item cho overlap
        List<String> overlapIndexes = itemsUpdate.stream().map(item -> item.incr(sumOverlapItemNo(items, item)))
                .map(HeroItem::getIndex).collect(Collectors.toList());

        // tạo item mới
        itemsUpdate
                .addAll(items.stream().filter(heroItem -> !overlapIndexes.contains(heroItem.getIndex())).peek(item -> {
                    item.setId(autoIncrService.genItemId());
                    item.setPlayerId(playerId);
                }).collect(Collectors.toList()));

        heroItemRep.saveAll(itemsUpdate);
//        //apply changed value for return to client
//        itemsUpdate.stream().filter(HeroItem::isOverlap).forEach(item -> item.setNo(sumOverlapItemNo(items, item)));
        return itemsUpdate;
    }


    private int sumOverlapItemNo(Collection<HeroItem> items, HeroItem item) {
        return items.stream().filter(heroItem -> heroItem.getIndex().equalsIgnoreCase(item.getIndex())).mapToInt(HeroItem::getNo)
                .sum();
    }

    //TODO nhớ lúc gọi trước buildUpdateRewardsReceipt vì sau khi hàm này gọi nó xóa cái assets đi
    public List<HeroItem> notifyAssetChange(QAntUser user, Collection<HeroItem> items) {
        if (items.size() == 0) {
            return null;
        }
        AtomicBoolean eggPiece = new AtomicBoolean(false);
        List<HeroItem> assetList = items.stream().filter(HeroItem::isBuildAssets).collect(Collectors.toList());
        Map<String, Integer> stringIntegerMap = new HashMap<>();
        if (assetList.size() > 0) {
            assetList.forEach(heroItem -> {
                int no = heroItem.getNo();
                String index = heroItem.getIndex();
                if (stringIntegerMap.containsKey(index)) {
                    no += stringIntegerMap.get(index);
                }
                stringIntegerMap.put(index, no);
            });
        }
        IQAntObject result = QAntObject.newInstance();
        QAntArray array = QAntArray.newInstance();
        stringIntegerMap.keySet().forEach(s -> {
            QAntObject object = QAntObject.newInstance();
            object.putUtfString("id", s);
            object.putInt("value", stringIntegerMap.get(s));
            array.addQAntObject(object);
            if (s.equals(EGG_PIECE)) {
                eggPiece.set(true);
            }
        });
        result.putQAntArray("assets", array);
        send(CMD_NTF_ASSETS_CHANGE, result, user);
        if (eggPiece.get()) {
            notifyEggpieceConvert(user);
        }
        return assetList;
    }

    private void notifyEggpieceConvert(QAntUser user) {
        try {
            Collection<HeroItem> itemEggPiece = getItemsByIndex(user.getName(), ItemRequestHandler.EGG_PIECE);
            if (itemEggPiece.size() > 0) {
                HeroItem item = itemEggPiece.stream().findFirst().get();
                if (item.getNo() >= ItemRequestHandler.EGG_PIECE_TO_EGE) {
                    int no = Math.floorMod(item.getNo(), ItemRequestHandler.EGG_PIECE_TO_EGE);
                    int noEgg = Math.floorDiv(item.getNo(), ItemRequestHandler.EGG_PIECE_TO_EGE);
                    IGenReward genRewards = new IGenReward() {
                        @Override
                        public String genRewards() {
                            return ItemRequestHandler.EGG + "/" + noEgg;
                        }

                        @Override
                        public List<RewardBase> genRewardsBase() {
                            return null;
                        }
                    };

                    IApplyRewards iApplyRewards = (rewards, wolRewardCompleteRes) -> {
                        item.setNo(no);
                        List<HeroItem> addItems = addItems(user, rewards);
                        OffChainServices.getInstance().applyOfcToItem(user.getName(), addItems, wolRewardCompleteRes);
                        addItems.add(item);
                        save(addItems);
                        IQAntObject result = new QAntObject();
                        QAntArray array = new QAntArray();
                        addItems.forEach(heroItem -> {
                            QAntObject object = new QAntObject();
                            object.putUtfString("id", heroItem.getIndex());
                            object.putInt("value", heroItem.getNo());
                            array.addQAntObject(object);
                        });
                        result.putQAntArray("assets", array);
                        send(CMD_NTF_ASSETS_CHANGE, result, user);
                    };
                    wolFlowManager.sendRewardRequest(user.getName(), genRewards, iApplyRewards, 10);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyAssetChange(QAntUser user) {
        List<HeroItem> consumableItems = getConsumableItem(user.getName());
        QAntTracer.debug(PlayerManager.class, consumableItems.toString());
        notifyAssetChange(user, consumableItems);
    }


    public HeroItem lockItem(String name, Long itemId, Boolean isLock) throws GameException {
        HeroEquipment equipment = getEquipment(itemId, name);
        if (equipment == null)
            throw new GameException();
        equipment.lockItem(isLock);
        return heroItemRep.save(equipment);
    }


//    public Collection<HeroItem> sellItems(QAntUser user, Map<Long, Integer> items) throws UseItemException {
//        Collection<HeroItem> collection = this.useItemsWithIds(user, items);
//        Map<String, Integer> refund = new ConcurrentHashMap<>();
//        collection.forEach(heroItem -> {
//            heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex()));
//            int itemNo = items.get(heroItem.getId());
//            String sellPrice = heroItem.getSellPrice();
//            if (!StringUtil.isNullOrEmpty(sellPrice))
//                for (int i = 1; i <= itemNo; i++) {
//                    ItemConfig.getInstance().convertToMap(refund, sellPrice);
//                }
//        });
//        Collection<HeroItem> heroItems = addItems(user.getName(), ItemConfig.getInstance().convertToHeroItem(refund));
//        heroItems.addAll(collection);
//        return heroItems;
//    }

    public Collection<HeroItem> openEgg(QAntUser user, Map<Long, Integer> items, IQAntObject params) throws UseItemException {
        Collection<HeroItem> collection = this.useItemsWithIds(user, items);
        Map<String, Integer> refund = new ConcurrentHashMap<>();
        Collection<HeroItem> heroItems = new ArrayList<>();
        IGenReward genRewards = new IGenReward() {
            @Override
            public String genRewards() {
                return RandomRangeUtil.randomDroprate(ItemConfig.getInstance().getEggRewards(), ItemConfig.getInstance().getEggRewardsRate(), 1, 1000);
            }

            @Override
            public List<RewardBase> genRewardsBase() {
                return null;
            }
        };
        IApplyUseItemRewards iApplyUpgrade = new IApplyUseItemRewards() {
            @Override
            public void applyRewards(String rewards, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject) {
                ItemConfig.getInstance().convertToMap(refund, rewards);
                Collection<HeroItem> items1 = ItemConfig.getInstance().convertToHeroItem(refund);
                OffChainServices.getInstance().applyOfcToItem(items1, wolAssetCompletedRes);
                save(heroItems);
                ItemConfig.getInstance().buildRewardsReceipt(params, refund.keySet().stream().map(heroItem -> heroItem + "/" + refund.get(heroItem)).collect(Collectors.joining("#")));
                List<HeroItem> heroItems1 = addItems(user.getName(), items1);
                heroItems.addAll(heroItems1);
                heroItems.addAll(collection);
            }

            @Override
            public void applyRewards(List<RewardBase> rewardBases, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject) {

            }
        };
        wolFlowManager.sendUseItemRequest(user.getName(), collection, genRewards, iApplyUpgrade);

        if (heroItems.size() == 0)
            throw new UseItemException();
        return heroItems;
    }

    public void openItem(String index, int itemNo, List<RewardBase> refund) {
        ItemBase item = itemConfig.getItem(index);
        List<RewardBase> rewardBases = ItemConfig.getInstance().getRewardBaseMap().get(index);
        if (item != null) {
            RewardBase rewardBase = rewardBases.get(rewardBases.size() - 1);
            int repeat = rewardBase.getRepeat();
            if (rewardBase.getRate() <= 0) {
                refund.addAll(rewardBases);
            } else {
                RandomRangeUtil.nRandomInRange(rewardBases.size(), itemNo * repeat).forEach(integer -> refund.add(rewardBases.get(integer)));
            }
        }
    }


    public Collection<HeroItem> getItemsByIds(String gameHeroId, Collection<Long> idsOn) {
        return heroItemRep.getItemByItemId(gameHeroId, idsOn);
    }

    public Collection<HeroItem> getItemsById(String gameHeroId, String index) {
        return heroItemRep.getHeroItemsByPlayerIdAndIndex(gameHeroId, index);
    }

    public Collection<HeroItem> getItemsByEquipFor(String gameHeroId, String index) {
        return heroItemRep.getAllByPlayerIdAndIndex(gameHeroId, index);
    }

    public Collection<HeroItem> getItemsByIndexs(String gameHeroId, Collection<String> idsOn) {
        return heroItemRep.getAllByPlayerIdAndIndexIsIn(gameHeroId, idsOn);
    }

    public Collection<HeroItem> getItemsByIndex(String gameHeroId, String index) {
        return heroItemRep.getAllByPlayerIdAndIndex(gameHeroId, index);
    }

    void resetItems(Player player) {
        Map<String, Integer> stringIntegerMap = itemConfig.splitItemToMaxValMap(PlayerManager.DAILY_ITEMS);
        Collection<HeroItem> items = itemConfig.splitItemToHeroItem(PlayerManager.DAILY_ITEMS);
        if (items == null || items.size() <= 0)
            return;
        String gameHeroId = player.getId();
        List<HeroItem> itemsUpdate = heroItemRep.getItemList(gameHeroId,
                items.stream().filter(HeroItem::isOverlap).map(HeroItem::getIndex).collect(Collectors.toSet()));

        // cập nhật số lượng item cho overlap
        itemsUpdate.forEach(item -> {
            int value = sumOverlapItemNo(items, item);
            int maxValue = stringIntegerMap.getOrDefault(item.getIndex(), value);
            value = Math.max(Math.max(value, maxValue), item.getNo());
            item.setNo(value);
        });
        heroItemRep.saveAll(itemsUpdate);
    }

    public void notifyItemChange(String gameHeroId, Collection<HeroItem> rewards, Collection<HeroItem> updateinfos) {
//        try {
//            QAntUser receiverUser = extension.getApi().getUserByName(gameHeroId);
//            IQAntObject antObject = new QAntObject();
//            antObject.putInt("act", ItemRequestHandler.NOTIFY_UPDATE);
//            ItemConfig.getInstance().buildUpdateRewardsReceipt(antObject, rewards);
//            ItemConfig.getInstance().buildItemUpdate(antObject, updateinfos);
//            send(ExtensionEvent.CMD_ITEM, antObject, receiverUser);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    public void useArenaTicket(QAntUser user, int i) throws GameException {

    }

    public void useDailyFreeTicket(QAntUser user, int i) {
    }

    public List<HeroItem> getAllItems(String id) {
        return heroItemRep.getAllItems(id);
    }

    public void offchainSync(String playerId, List<WolAsset> items) {
        List<HeroItem> allItems = heroItemRep.getAllItems(playerId);
        List<HeroItem> itemsRemove = new ArrayList<>();
        List<String> ids = items.stream().map(WolAsset::getOfcId).collect(Collectors.toList());
        List<String> idsCreated = new ArrayList<>();
        allItems.forEach(item -> {
            String ofcId = item.getOfcId();
            if (Utils.isNullOrEmpty(ofcId)) {
                if (isOffchainItem(item)) {
                    itemsRemove.add(item);
                }
            } else {
                if (!ids.contains(ofcId)) {
                    itemsRemove.add(item);
                } else {
                    idsCreated.add(ofcId);
                }
            }
        });
        PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
        items.forEach(wolAsset -> {
            if (!idsCreated.contains(wolAsset.getOfcId())) {
                playerManager.updateOffchainAssets(playerId, wolAsset);
            }
        });
        remove(itemsRemove);
    }

    private boolean isOffchainItem(HeroItem heroItem) {
        if (heroItem instanceof HeroConsumeItem) {
            return heroItem.getIndex().equals(EGG) || heroItem.getIndex().equals(STARTER);
        } else return heroItem instanceof HeroEquipment;
    }
}
