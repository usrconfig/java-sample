package com.seagame.ext.managers;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.controllers.ExtensionEvent;
import com.seagame.ext.dao.HeroItemRepository;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.item.HeroEquipment;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.item.ItemBase;
import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.exception.UseItemException;
import com.seagame.ext.services.AutoIncrementService;
import com.seagame.ext.util.NetworkConstant;
import com.seagame.ext.util.RandomRangeUtil;
import io.netty.util.internal.StringUtil;
import org.apache.commons.collections.CollectionUtils;
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
                .forEach(heroItem -> heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex())));
        return itemPage;
    }


    public Page<HeroItem> getItemInCofferExceptMoneyType(String gameHeroId, int page) {
        Page<HeroItem> itemPage = heroItemRep.getItemInCoffer(gameHeroId, PageRequest.of(page - 1, MAX_ITEM_PER_PAGE));
        itemPage.getContent()
                .forEach(heroItem -> heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex())));

        IQAntArray arr = QAntArray.newInstance();
        itemPage.getContent().forEach(item -> arr.addQAntObject(QAntObject.newFromObject(item)));
        System.out.println(arr.getDump());
        return itemPage;
    }

    public List<HeroItem> getConsumableItem(String gameHeroId) {
        return heroItemRep.getConsumableItem(gameHeroId);
    }


    public List<HeroItem> save(Collection<HeroItem> items) {
        return heroItemRep.saveAll(items);
    }

    public HeroItem save(HeroItem items) {
        return heroItemRep.save(items);
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
        items.forEach(heroItem -> heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex())));
        return items;
    }


    public HeroEquipment getEquipment(long itemId, String gameHeroId) {
        HeroEquipment equipment = heroItemRep.getEquipment(itemId, gameHeroId);
        if (equipment != null)
            equipment.setItemBase(itemConfig.getItem(equipment.getIndex()));
        return equipment;
    }


    public List<HeroItem> putItemInToCoffer(String gameHeroId, Collection<Long> itemIds, boolean isPutIn) {
        List<HeroItem> itemList = heroItemRep.getItemByItemId(gameHeroId, itemIds);
        itemList.forEach(item -> {
            item.putInCoffer(isPutIn);
            item.setItemBase(itemConfig.getItem(item.getIndex()));
        });

        if (itemList.size() <= 0)
            return new ArrayList<>();
        return heroItemRep.saveAll(itemList);
    }


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
        if (consumeAbleItems.size() != itemMap.size())
            throw UseItemException.lackOfItem();

        Optional<HeroItem> lackOfMaterial = consumeAbleItems.stream()
                .filter(item -> item.decr(itemMap.get(item.getIndex())) < 0).findFirst();
        if (lackOfMaterial.isPresent())
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


    public void update(HeroItem heroItem) {
        if (heroItem.getNo() < 0)
            heroItemRep.delete(heroItem);
        else
            heroItemRep.save(heroItem);
    }


    public void update(Collection<HeroItem> heroItems) {
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
        if (assetList.size() > 0) {
            IQAntObject result = QAntObject.newInstance();
            QAntArray array = QAntArray.newInstance();
            assetList.forEach(item -> {
                QAntObject object = QAntObject.newInstance();
                object.putUtfString("id", item.getIndex());
                object.putInt("value", item.getNo());
                array.addQAntObject(object);
                if (item.getIndex().equals("9910")) {
                    eggPiece.set(true);
                }
            });
            result.putQAntArray("assets", array);
            send(CMD_NTF_ASSETS_CHANGE, result, user);

            if (eggPiece.get()) {
                notifyEggpieceConvert(user);
            }
        }
        return assetList;
    }

    private void notifyEggpieceConvert(QAntUser user) {
        try {
            Collection<HeroItem> itemEgg = getItemsByIndex(user.getName(), "9910");
            Collection<HeroItem> itemEggPiece = getItemsByIndex(user.getName(), "9911");
            if (itemEgg.size() > 0 && itemEggPiece.size() > 0) {
                HeroItem eggpiece = itemEggPiece.stream().findFirst().get();
                HeroItem egg = itemEgg.stream().findFirst().get();
                int no = eggpiece.getNo();
                if (no >= 100) {
                    eggpiece.setNo(no % 100);
                    egg.setNo(egg.getNo() + no / 100);
                }
                heroItemRep.saveAll(itemEgg);
                heroItemRep.saveAll(itemEggPiece);
                itemEgg.addAll(itemEggPiece);
                notifyAssetChange(user, itemEgg);
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


    public Collection<HeroItem> sellItems(QAntUser user, Map<Long, Integer> items) throws UseItemException {
        Collection<HeroItem> collection = this.useItemsWithIds(user, items);
        Map<String, Integer> refund = new ConcurrentHashMap<>();
        collection.forEach(heroItem -> {
            heroItem.setItemBase(itemConfig.getItem(heroItem.getIndex()));
            int itemNo = items.get(heroItem.getId());
            String sellPrice = heroItem.getSellPrice();
            if (!StringUtil.isNullOrEmpty(sellPrice))
                for (int i = 1; i <= itemNo; i++) {
                    ItemConfig.getInstance().convertToMap(refund, sellPrice);
                }
        });
        Collection<HeroItem> heroItems = addItems(user.getName(), ItemConfig.getInstance().convertToHeroItem(refund));
        heroItems.addAll(collection);
        return heroItems;
    }

    public Collection<HeroItem> openEgg(QAntUser user, Map<Long, Integer> items, IQAntObject params) throws UseItemException {
        Collection<HeroItem> collection = this.useItemsWithIds(user, items);
        Map<String, Integer> refund = new ConcurrentHashMap<>();
        String rewards = RandomRangeUtil.randomDroprate(ItemConfig.getInstance().getEggRewards(), ItemConfig.getInstance().getEggRewardsRate(), 1, 100);
        ItemConfig.getInstance().convertToMap(refund, rewards);
        Collection<HeroItem> items1 = ItemConfig.getInstance().convertToHeroItem(refund);
        ItemConfig.getInstance().buildRewardsReceipt(params, refund.keySet().stream().map(heroItem -> heroItem + "/" + refund.get(heroItem)).collect(Collectors.joining("#")));
        Collection<HeroItem> heroItems = addItems(user.getName(), items1);
        heroItems.addAll(collection);
        return heroItems;
    }

    public void openItem(String index, int itemNo, List<RewardBase> refund) {
        ItemBase item = itemConfig.getItem(index);
        List<RewardBase> rewardBases = ItemConfig.getInstance().getRewardBaseMap().get(index);
        if (item != null) {
            RandomRangeUtil.nRandomInRange(rewardBases.size(), itemNo).forEach(integer -> refund.add(rewardBases.get(integer)));
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
}
