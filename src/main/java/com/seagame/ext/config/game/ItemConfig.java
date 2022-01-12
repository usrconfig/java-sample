package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.item.*;
import com.seagame.ext.managers.HeroClassManager;
import com.seagame.ext.managers.HeroItemManager;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.quest.QuestSystem;
import com.seagame.ext.util.NetworkConstant;
import com.seagame.ext.util.RandomRangeUtil;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LamHM
 */
@Getter
public class ItemConfig implements NetworkConstant {
    public static final String ITEM_CONFIG = "items.xml";
    private static ItemConfig instance;
    private Map<String, ItemBase> itemMap;
    private Map<String, EquipBase> equipMap;
    private Map<String, EggRewardBase> eggRewardMap;
    private Map<String, List<RewardBase>> rewardBaseMap;
    private Map<String, List<EquipRankBase>> ranksEquipMap;
    private Items items;
    private String eggRewards;
    private String eggRewardsRate;

    public static final String GOLD_REWARDS = "ITEM001/";


    public static ItemConfig getInstance() {
        if (instance == null)
            instance = new ItemConfig();

        return instance;
    }


    private ItemConfig() {
        itemMap = new HashMap<>();
        equipMap = new HashMap<>();
        eggRewardMap = new HashMap<>();
        rewardBaseMap = new HashMap<>();
        ranksEquipMap = new HashMap<>();
        loadItems();
    }

    public static String buildReward(String rewards) {
        String reward = RandomRangeUtil.randomReward(rewards, 1);
        return RandomRangeUtil.randomQuantity(reward);
    }


    void validateItem(String itemReward) {
        ItemConfig itemConfig = ItemConfig.getInstance();
        try {
            Collection<String> itemIndexList = itemConfig.convertToMap(itemReward).keySet();
            for (String index : itemIndexList) {
                ItemBase item = itemConfig.getItem(index);
                if (item == null) {
                    QAntTracer.error(this.getClass(),
                            "[ERROR] ******************** ITEM NOT FOUND: " + index + "/ItemString:" + itemReward);
                }
            }
        } catch (Exception e) {
            QAntTracer.error(this.getClass(), "[ERROR] validate item: " + itemReward);
        }

    }

    private boolean validateItem(List<Integer> indexList) {
        boolean itemValid = true;
        for (Integer index : indexList) {
            if (itemMap.get(index) == null) {
                QAntTracer.error(this.getClass(), "[ERROR] ***************** ITEM NOT FOUND: " + index);
                itemValid = false;
            }
        }
        return itemValid;
    }

    private void loadItems() {
        try {
            XMLStreamReader sr = SourceFileHelper.getStreamReader(ITEM_CONFIG);
            XmlMapper mapper = new XmlMapper();
            items = mapper.readValue(sr, Items.class);
            items.getRewards().forEach(rewardBase -> {
                rewardBaseMap.putIfAbsent(rewardBase.getRewardsID(), new ArrayList<>());
                rewardBaseMap.get(rewardBase.getRewardsID()).add(rewardBase);
            });
            items.getItems().forEach(itemBase -> {
                if (rewardBaseMap.containsKey(itemBase.getId())) {
                    itemBase.setRewards(rewardBaseMap.get(itemBase.getId()));
                }
                itemMap.put(itemBase.getId(), itemBase);
            });
            items.getRanks().forEach(rankBase -> {
                ranksEquipMap.putIfAbsent(rankBase.getID(), new ArrayList<>());
                ranksEquipMap.get(rankBase.getID()).add(rankBase);
            });
            items.getEquips().forEach(itemBase -> {
                if (ranksEquipMap.containsKey(itemBase.getId())) {
                    itemBase.setRanks(ranksEquipMap.get(itemBase.getId()));
                }
                equipMap.put(itemBase.getId(), itemBase);
            });
            ArrayList<String> eggRws = new ArrayList<>();
            ArrayList<String> eggRwsRate = new ArrayList<>();
            items.getEggRewards().forEach(itemBase -> {
                eggRewardMap.put(itemBase.getIndex(), itemBase);
                eggRws.add(itemBase.getReward() + "/" + itemBase.getCount());
                eggRwsRate.add(String.valueOf(itemBase.getRate()));
            });
            eggRewards = String.join("#", eggRws);
            eggRewardsRate = String.join("#", eggRwsRate);
            sr.close();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }

    public List<ShortItemExt> splitItem(String itemArrString) {
        return convertToItem(Arrays.stream(StringUtils.split(itemArrString, SEPERATE_OTHER_ITEM))
                .map(itemString -> StringUtils.split(itemString, SEPERATE_ITEM_NO))
                .filter(itemArr -> itemArr.length >= 2)
                .map(split -> new String[]{split[0], split[1]})
                .collect(Collectors.toList()));
    }


    public Collection<HeroItem> splitItemToHeroItem(String itemArrString) {
        Map<String, Integer> itemsRewardMap = new ConcurrentHashMap<>();
        if (StringUtils.isEmpty(itemArrString))
            return convertToHeroItem(itemsRewardMap);

        if (StringUtils.isNotBlank(itemArrString)) {
            String[] items = StringUtils.split(itemArrString, SEPERATE_OTHER_ITEM);
            for (String item : items) {
                String[] split = StringUtils.split(item, SEPERATE_ITEM_NO);
                String key = split[0];
                int value = Integer.parseInt(split[1]);
                if (itemsRewardMap.containsKey(key)) {
                    value += itemsRewardMap.get(key);
                }
                itemsRewardMap.put(key, value);
            }
        }
        return convertToHeroItem(itemsRewardMap);
    }

    public Map<String, Integer> splitItemToMaxValMap(String itemArrString) {
        Map<String, Integer> itemsRewardMap = new ConcurrentHashMap<>();
        if (StringUtils.isEmpty(itemArrString))
            return itemsRewardMap;

        if (StringUtils.isNotBlank(itemArrString)) {
            String[] items = StringUtils.split(itemArrString, SEPERATE_OTHER_ITEM);
            for (String item : items) {
                String[] split = StringUtils.split(item, SEPERATE_ITEM_NO);
                String key = split[0];
                if (split[1] != null) {
                    int value = Integer.parseInt(split[1]);
                    if (itemsRewardMap.containsKey(key)) {
                        value += itemsRewardMap.get(key);
                    }
                    itemsRewardMap.put(key, value);
                }
            }
        }
        return itemsRewardMap;
    }

    public List<String> splitRewardString(String rewardString) {
        return splitItemString(rewardString);
    }


    public List<String> splitItemString(String itemString) {
        if (StringUtils.isNotBlank(itemString))
            return Arrays.stream(StringUtils.split(itemString, SEPERATE_OTHER_ITEM)).collect(Collectors.toList());

        return new ArrayList<>();
    }


    public Map<String, Integer> convertToMap(String itemString) {
        return convertToMap(new ConcurrentHashMap<>(), itemString);
    }

    public String parseItemArrayToString(IQAntArray array) {
        int size = array.size();
        StringBuilder itemString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                itemString.append(NetworkConstant.SEPERATE_OTHER_ITEM);
            }
            IQAntObject itemObj = array.getQAntObject(i);
            itemString.append(itemObj.getLong("id"));
            itemString.append(NetworkConstant.SEPERATE_ITEM_NO);
            itemString.append(itemObj.getInt("no"));
        }
        return itemString.toString();
    }

    public Map<String, Integer> convertToMap(Map<String, Integer> result, String itemString) {
        String[] split = StringUtils.split(itemString, SEPERATE_OTHER_ITEM);
        for (String aSplit : split) {
            String[] items = StringUtils.split(aSplit, SEPERATE_ITEM_NO);
            String index = items[0];
            Integer no = result.get(index);
            if (no == null)
                no = 0;

            result.put(index, Integer.parseInt(items[1]) + no);
        }

        return result;
    }


    private QAntArray buildShortItemInfo(Collection<HeroItem> itemList) {
        QAntArray items = QAntArray.newInstance();
        QAntObject obj;
        for (HeroItem heroItem : itemList) {
            obj = QAntObject.newInstance();
            obj.putUtfString("index", heroItem.getIndex());
            obj.putInt("no", heroItem.getNo());
            items.addQAntObject(obj);
        }
        return items;
    }


    public QAntArray buildShortItemInfo(String itemArrString) {
        return buildShortItemInfo(splitItemToHeroItem(itemArrString));
    }


    private List<ShortItemExt> convertToItem(List<String[]> items) {
        List<ShortItemExt> itemList = new ArrayList<>();
        if (items.size() > 0) {
            for (String[] ir : items) {
                ItemBase itemBase = getItem(ir[0]);
                ShortItemExt item = new ShortItemExt();
                item.setNo(Integer.parseInt(ir[1]));
                item.setIndex(itemBase.getId());
                itemList.add(item);
            }
        }
        return itemList;
    }


    public Collection<HeroItem> convertToHeroItem(Map<String, Integer> items) {
        List<HeroItem> itemList = new ArrayList<>();

        items.forEach((key, value) -> {
            ItemBase itemBase = getItem(key);
            if (itemBase != null) {
                switch (itemBase.getType()) {
                    case ITEM_CURRENCY: {
                        HeroConsumeItem heroConsumeAble = new HeroConsumeItem(itemBase);
                        heroConsumeAble.setNo(value);
                        itemList.add(heroConsumeAble);
                        break;
                    }
                    case ITEM_TICKET:
                    case ITEM_REWARDS:
                    case ITEM_POINT:
                    case ITEM_MATERIAL:
                        HeroConsumeItem heroConsumeAble = new HeroConsumeItem(itemBase);
                        heroConsumeAble.setNo(value);
                        itemList.add(heroConsumeAble);
                        break;
                    default:
                        for (int i = 0; i < value; i++) {
                            HeroEquipment heroEquipment = new HeroEquipment(itemBase);
                            heroEquipment.setNo(1);
                            itemList.add(heroEquipment);
                        }
                        break;
                }
            }
        });
        return itemList;
    }

    public void writeToJsonFile() {
        try {
            exportItems();
            exportEquips();
            exportEggRewards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String exportItems() throws Exception {
        return SourceFileHelper.exportJsonFile(itemMap.values(), "items.json");
    }

    public String exportEquips() throws Exception {
        return SourceFileHelper.exportJsonFile(equipMap.values(), "equips.json");
    }

    public String exportEggRewards() throws Exception {
        return SourceFileHelper.exportJsonFile(eggRewardMap.values(), "eggRewards.json");
    }

//    public String exportItemExp() throws Exception {
//        return SourceFileHelper.exportJsonFile(items.getExpInfos(), "itemExp.json");
//    }

    public ItemBase getItem(String index) {
        return itemMap.get(index);
    }

    public ItemBase getEquip(String index) {
        return equipMap.get(index);
    }

    public int getEquipPower(String index, int rank, int level) {
        try {
            return getRanksEquipMap().get(index).get(rank).getPower(level);
        } catch (Exception e) {

        }
        return 0;
    }


    public List<ItemBase> getItems() {
        return new ArrayList<>(itemMap.values());
    }


    public static void main(String[] args) throws Exception {
        ItemConfig.getInstance().writeToJsonFile();
    }

    public void initItemBase(Collection<HeroItem> heroItems) {
        heroItems.forEach(heroItem -> heroItem.setItemBase(ItemConfig.getInstance().getItem(heroItem.getIndex())));
    }

    public void buildUseReceipt(IQAntObject params, PlayerManager playerManager, Collection<HeroItem> useItems, Collection<HeroClass> useHeroes) {
        QAntObject updateObj = QAntObject.newInstance();
        if (useItems != null && useItems.size() > 0) {
            ItemConfig.getInstance().initItemBase(useItems);
            if (playerManager == null) {
                playerManager = ExtApplication.getBean(PlayerManager.class);
            }

            HeroItemManager heroItemManager = ExtApplication.getBean(HeroItemManager.class);
            heroItemManager.update(useItems);
            List<HeroItem> assetList = useItems.stream().filter(HeroItem::isBuildAssets).collect(Collectors.toList());
            if (assetList.size() > 0) {
                useItems.removeAll(assetList);
                IQAntArray assets = QAntArray.newInstance();
                assetList.forEach(heroEquipment -> assets.addQAntObject(heroEquipment.buildShortInfo()));
                updateObj.putQAntArray(KEYQA_ASSETS, assets);
            }
            Collection<Long> items = useItems.stream().map(HeroItem::getId).collect(Collectors.toList());
            updateObj.putLongArray(KEYLA_ITEMS, items);
        }
        if (useHeroes != null && useHeroes.size() > 0) {
            HeroClassManager heroClassManager = ExtApplication.getBean(HeroClassManager.class);
            heroClassManager.remove(useHeroes);
            List<Long> heroes = useHeroes.stream().map(HeroClass::getId).collect(Collectors.toList());
            updateObj.putLongArray(KEYLA_HEROES, heroes);
        }
        if (params != null)
            params.putQAntObject(KEYQO_UPDATE, updateObj);

    }

    public void buildUpdateRewardsReceipt(IQAntObject params, Collection<HeroItem> items) {
        if (items != null) {
            buildItemsReceipt(params, items.stream().filter(heroItem -> !heroItem.isBuildAssets()).collect(Collectors.toList()), "updates");
        }
    }

    public Collection<HeroItem> buildRewardsReceipt(IQAntObject params, String items) {
        Collection<HeroItem> items1 = ItemConfig.getInstance().splitItemToHeroItem(items);
        buildItemsReceipt(params, items1, "rewards");
        return items1;
    }

    public void buildRewardsSkinReceipt(IQAntObject params, Map<String, Map<String, Integer>> items) {
        QAntObject updateSkin = QAntObject.newInstance();
        if (items != null) {
            items.keySet().forEach(s -> {
                QAntArray qAntArray = new QAntArray();
                Map<String, Integer> stringIntegerMap = items.get(s);
                stringIntegerMap.keySet().forEach(s1 -> {
                    QAntObject skin = new QAntObject();
                    skin.putUtfString("id", s1);
                    skin.putInt("no", stringIntegerMap.get(s1));
                    qAntArray.addQAntObject(skin);
                });
                updateSkin.putQAntArray(s, qAntArray);
            });
        }
        params.putQAntObject("skinRewards", updateSkin);
    }

    public void buildItemsReceipt(IQAntObject params, Collection<HeroItem> items, String key) {
        QAntArray updateArray = QAntArray.newInstance();
        if (items != null) {
            items.forEach(item -> updateArray.addQAntObject(item.buildInfo()));
        }
        params.putQAntArray(key, updateArray);
    }

    public void buildItemUpdate(IQAntObject params, Collection<HeroItem> items) {
        QAntArray updateArray = QAntArray.newInstance();
        if (items != null) {
            items.forEach(item -> updateArray.addQAntObject(item.buildInfo()));
        }
        params.putQAntArray("items", updateArray);
    }


    private String buildItems(String split) {
        return Stream.of(split.split(NetworkConstant.SEPERATE_OTHER_ITEM)).filter(s -> {
            String[] part = s.split(SEPERATE_ITEM_NO);
            return part.length == 2;
        }).map(s -> {
            String[] part = s.split(SEPERATE_ITEM_NO);
            return part[0] + SEPERATE_ITEM_NO + part[1];
        }).collect(Collectors.joining(SEPERATE_OTHER_ITEM));
    }

    public void addItem(Collection<HeroItem> heroItems, HeroItem item) {
        if (item instanceof HeroConsumeItem) {
            AtomicBoolean found = new AtomicBoolean(false);
            heroItems.forEach(item1 -> {
                if (!found.get() && item1.getIndex().equals(item.getIndex())) {
                    item1.no += item.getNo();
                    found.set(true);
                }
            });
            if (!found.get()) {
                heroItems.add(item);
            }
        } else {
            heroItems.add(item);
        }
    }

//    public void buildReceivedRewards(String rewards, PlayerManager playerManager, String gameHeroId) {
//        if (StringUtils.isNotBlank(rewards)) {
//            ReceiptInfo receiptInfo = ReceiptManager.buildReceipt(rewards);
//            buildReceivedRewards(receiptInfo, playerManager, gameHeroId);
//        }
//    }

//    public void buildReceivedRewards(ReceiptInfo receiptInfo, PlayerManager playerManager, String gameHeroId) {
//        if (receiptInfo != null) {
//            Collection<HeroItem> heroItems = ItemConfig.getInstance().splitItemToHeroItem(receiptInfo.getItemReceipt());
//            heroItems.forEach(heroItem -> getQuestSystem().notifyObservers(CollectionTask.init(gameHeroId, heroItem.getIndex(), heroItem.getNo())));
//            Collection<HeroItem> items = playerManager.getHeroItemManager().addItems(gameHeroId, heroItems);
//            playerManager.getHeroItemManager().notifyItemChange(gameHeroId, heroItems, items);
//        }
//    }


    public String getRanDomAttackItem() {
        List<ItemBase> attack = getItems().stream().filter(itemBase -> itemBase.getType().equals("attack")).collect(Collectors.toList());
        Collections.shuffle(attack);
        return attack.get(0).getId();
    }

    public String getRanDomSupportItem() {
        List<ItemBase> attack = getItems().stream().filter(itemBase -> itemBase.getType().equals("support")).collect(Collectors.toList());
        Collections.shuffle(attack);
        return attack.get(0).getId();
    }

    public String getEggRewards() {
        return String.join("#", eggRewards);
    }

    public String getEggRewardsRate() {
        return String.join("#", eggRewardsRate);
    }

//    public String buildRewardsForMonster(String monster, int level, PlayerManager playerManager, String playerId, boolean isBoss) {
//        getQuestSystem().notifyObservers(KillTask.init(playerId, monster, 1));
//        MonsterBase monsterBase = MonsterConfig.getInstance().getMonster(monster);
//        String drops = monsterBase.getRoido();
//        return Arrays.stream(RandomRangeUtil.randomRewardV2(drops, 1).split("#")).map(s -> s + "/" + (new Random().nextInt(10) + 1) + "/" + getLevelItemFromMonster(s, level)
//        ).collect(Collectors.joining("#")) + ("#im5" + "/" + (new Random().nextInt(3) + 1) + "/" + getLevelItemFromMonster("im5", level));
//    }

//    private int getLevelItemFromMonster(String s, int level) {
//        Map<String, Integer> itemLevelMap = expMap.get(level);
//        return itemLevelMap != null ? itemLevelMap.getOrDefault(s, 0) : 1;
//    }


}
