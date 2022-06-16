//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.seagame.ext.offchain.services;

import com.creants.creants_2x.core.util.QAntTracer;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.Utils;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.config.game.ItemConfig;
import com.seagame.ext.config.game.SysConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.hero.HeroClass;
import com.seagame.ext.entities.item.*;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.offchain.entities.*;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Getter
public class OffChainServices implements InitializingBean, NetworkConstant {

    private static final String OFFCHAIN_URL = "https://dev.api.battlecity.io/";
    private static final String API_KEY = "YV8Np3404Ynf4rb6qF9J";
    private static final String SYSTEM_KEY = "battle_city";
    private static final String GAME_KEY = "battle_city_mu";


    private static final String URL_GAME = "game/";
    private static final String URL_AUTH = "auth/";
    private static final String SUB_PLAYER = "player/";
    private static final String SUB_ASSET = "asset/";
    private static final String SUB_REWARD = "reward/";
    private static final String ACT_VERIFY = "account-info/";
    private static final String ACT_GET_BALANCE = SUB_PLAYER + "get-info/";
    private static final String ACT_EXCHANGE_RATE = "get-exchange-rate/";
    private static final String ACT_UPDATE_BALANCE = SUB_PLAYER + "update-balance/";
    private static final String ACT_ASSET_CHECK_STOCK = SUB_ASSET + "check-stock/";
    private static final String ACT_ASSET_COMPLETED = SUB_ASSET + "completed/";
    private static final String ACT_ASSET_UPGRADE = SUB_ASSET + "upgrade/";
    private static final String ACT_REWARD_CHECK_STOCK = SUB_REWARD + "check-stock/";
    private static final String ACT_REWARD_COMPLETED = SUB_REWARD + "completed/";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final int MAX_CONNECTION_POOL = 100;
    private static OffChainServices instance;
    private CloseableHttpClient client;
    Map<String, WolRewardCompleteReq> wolRewardCheckStockRess;
    private static final Timer timer = new Timer();

    public static OffChainServices getInstance() {
        if (instance == null) {
            instance = new OffChainServices();
        }

        return instance;
    }

    private OffChainServices() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        this.client = HttpClients.custom().setConnectionManager(cm).build();
        cm.setDefaultMaxPerRoute(100);
        wolRewardCheckStockRess = new HashMap<String, WolRewardCompleteReq>();
    }

    private String doPostOffChain(List<NameValuePair> formReq, String action) {
        return this.doPostRequest(formReq, URL_GAME + action);
    }

    private String doPostRequest(List<NameValuePair> formReq, String url) {
        int statusCode = -1;
        try {
            Request bodyForm = Request.Post(url).addHeader("Content-Type", "application/x-www-form-urlencoded").connectTimeout(CONNECT_TIMEOUT).socketTimeout(SOCKET_TIMEOUT).bodyForm(formReq);
            HttpResponse httpResponse = Executor.newInstance(this.client).execute(bodyForm).returnResponse();
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            }
        } catch (Exception var6) {
            QAntTracer.error(this.getClass(), new Object[]{"doPostRequest! url: " + url + ", tracer:" + QAntTracer.getTraceMessage(var6)});
        }

        throw new RuntimeException("Failed : HTTP error code " + statusCode + ", url:" + url);
    }

    private JSONObject doPostOffChain(String formReq, String action) throws IOException {
        return this.doPostRequest(formReq, URL_GAME + action, null, null);
    }

    private JSONObject doPostOffChainAuth(String formReq, String action, OffChainResponseHandler offChainResponseHandler, String token) throws IOException {
        return this.doPostRequest(formReq, URL_AUTH + action, offChainResponseHandler, token);
    }

    private JSONObject doPostOffChain(String formReq, String action, OffChainResponseHandler offChainResponseHandler) throws IOException {
        return this.doPostRequest(formReq, URL_GAME + action, offChainResponseHandler, null);
    }

    private JSONObject doPostOffChain(String formReq, String action, OffChainResponseHandler offChainResponseHandler, String token) throws IOException {
        return this.doPostRequest(formReq, URL_GAME + action, offChainResponseHandler, token);
    }

    private JSONObject doPostRequest(String json, String url, OffChainResponseHandler offChainResponseHandler, String token) throws IOException {
        url = SysConfig.getInstance().getKeyCfg("offchain.url", OFFCHAIN_URL) + url;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        if (token != null) {
            httpPost.setHeader("Authorization", "Bearer " + token);
        }

        CloseableHttpResponse response = client.execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            String s = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (!Utils.isNullOrEmpty(s)) {
                JSONObject info = JSONObject.fromObject(s);
                if (info.getString("status").equals("OK")) {
                    JSONObject result = info.getJSONObject("result");
                    if (offChainResponseHandler != null)
                        return offChainResponseHandler.onOk(result);
                    return result;
                } else {
                    QAntTracer.warn(OffChainServices.class, "!!!!!!!!!!!!Begin!!!!!!!!!!!!!!");
                    QAntTracer.warn(OffChainServices.class, "Json : " + json);
                    QAntTracer.warn(OffChainServices.class, "Url : " + url);
                    QAntTracer.warn(OffChainServices.class, "Result : " + info.toString());
                    QAntTracer.warn(OffChainServices.class, "!!!!!!!!!!!!!End!!!!!!!!!!!!!");
                    if (offChainResponseHandler != null)
                        return offChainResponseHandler.onNg(info);
                    return info;
                }
            }
            throw new RuntimeException("Failed : HTTP error code " + statusCode + ", url:" + url + ", json:" + json);
        }
        client.close();
        throw new RuntimeException("Failed : HTTP error code " + statusCode + ", url:" + url);
    }

    private WolReq createFormMU(WolReq wolReq) {
        wolReq.setApi_key(SysConfig.getInstance().getKeyCfg("offchain.api_key", API_KEY));
        wolReq.setGame(SysConfig.getInstance().getKeyCfg("offchain.system", GAME_KEY));
        wolReq.setSystem(SysConfig.getInstance().getKeyCfg("offchain.game", SYSTEM_KEY));
        return wolReq;
    }

    public WolPlayerRes getInfo(String address) throws IOException {
        WolCheckBalanceReq wolPlayerBalanceReq = new WolCheckBalanceReq();
        wolPlayerBalanceReq.setAddress(address);
        JSONObject s = this.doPostOffChain(createFormMU(wolPlayerBalanceReq).build(), ACT_GET_BALANCE);
        return new WolPlayerRes().init(s);
    }

    public JSONObject getExchangeRate() throws IOException {
        WolGetExchangeRateReq wolGetExchangeRateReq = new WolGetExchangeRateReq();
        return this.doPostOffChain(createFormMU(wolGetExchangeRateReq).build(), ACT_EXCHANGE_RATE);
    }

    public JSONObject verifyToken(String token) throws IOException {
        return this.doPostOffChainAuth(createFormMU(new WolVerifyReq()).build(), ACT_VERIFY, null, token);
    }

    public JSONObject checkStockAsset(WolAssetCheckStockReq wolAssetCheckStockReq, OffChainResponseHandler checkStock) throws IOException {
        return this.doPostOffChain(createFormMU(wolAssetCheckStockReq).build(), ACT_ASSET_CHECK_STOCK, checkStock);
    }

    public JSONObject assetCompleted(WolAssetCompletedReq assetCompletedReq) throws IOException {
        return this.doPostOffChain(createFormMU(assetCompletedReq).build(), ACT_ASSET_COMPLETED);
    }

    public JSONObject assetUpgrade(WolAssetUpgradeReq wolAssetUpgradeReq, OffChainResponseHandler checkStock) throws IOException {
        return this.doPostOffChain(createFormMU(wolAssetUpgradeReq).build(), ACT_ASSET_UPGRADE, checkStock);
    }


    public JSONObject checkStockReward(WolRewardCheckStockReq wolRewardCheckStockReq, OffChainResponseHandler checkStock) throws IOException {
        return this.doPostOffChain(createFormMU(wolRewardCheckStockReq).build(), ACT_REWARD_CHECK_STOCK, checkStock);
    }

    public WolRewardCompleteRes rewardCompleted(WolRewardCompleteReq wolRewardCompleteReq) throws IOException {
        JSONObject s = this.doPostOffChain(createFormMU(wolRewardCompleteReq).build(), ACT_REWARD_COMPLETED);
        return new WolRewardCompleteRes().init(s);
    }

    public static void main(String[] args) {

    }

    public void assetFlow(String playerAddress, ArrayList<WolAsset> assets, OffChainResponseHandler checkStock) {
        try {
            WolPlayerRes x000001 = this.getInfo(playerAddress);
            WolAssetCheckStockReq wolAssetCheckStockReq = new WolAssetCheckStockReq();
            wolAssetCheckStockReq.setPlayer(x000001);
            wolAssetCheckStockReq.setAssets(assets);
            JSONObject jsonObject = this.checkStockAsset(wolAssetCheckStockReq, checkStock);
            if (jsonObject == null) {
                return;
            }
            WolAssetCheckStockRes wolAssetCheckStockRes = new WolAssetCheckStockRes().init(jsonObject);
            WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
            wolAssetCompletedReq.setPlayer(wolAssetCheckStockRes.getPlayer());
            wolAssetCompletedReq.setAssets(wolAssetCheckStockRes.getAssets());
            JSONObject jsonObject1 = this.assetCompleted(wolAssetCompletedReq);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateBalanceFlow(String playerAddress, String cost, OffChainResponseHandler offChainResponseHandler) {
        AtomicInteger ken = new AtomicInteger();
        AtomicInteger wol = new AtomicInteger();
        buildWolAssets(cost, wol, ken);
        updateBalanceFlow(playerAddress, wol.get(), ken.get(), offChainResponseHandler);
    }

    public void updateBalanceFlow(String playerAddress, int wol, int ken, OffChainResponseHandler offChainResponseHandler) {
        try {
            WolPlayerRes x000001 = this.getInfo(playerAddress);
            WolUpdateBalanceReq wolUpdateBalanceReq = new WolUpdateBalanceReq();
            wolUpdateBalanceReq.setPlayer(x000001);
            wolUpdateBalanceReq.setKen(-ken);
            wolUpdateBalanceReq.setWol(-wol);
            JSONObject jsonObject = this.updateBalance(wolUpdateBalanceReq, offChainResponseHandler);
            if (jsonObject == null) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject updateBalance(WolUpdateBalanceReq wolUpdateBalanceReq, OffChainResponseHandler offChainResponseHandler) throws IOException {
        return this.doPostOffChain(createFormMU(wolUpdateBalanceReq).build(), ACT_UPDATE_BALANCE, offChainResponseHandler);
    }

    public void rewardFlowCheckStock(List<WolRewardPlayer> wolRewardPlayers, OffChainResponseHandler checkStock) {
        try {
            WolRewardCheckStockReq wolRewardCheckStockReq = new WolRewardCheckStockReq();
            List<WolAsset> assets = new ArrayList<>();
            AtomicInteger totalKenRewards = new AtomicInteger(0);
            AtomicInteger totalWolRewards = new AtomicInteger(0);
            wolRewardPlayers.forEach(wolRewardPlayer -> {
                totalKenRewards.addAndGet(wolRewardPlayer.getPlayer().getKen());
                totalWolRewards.addAndGet(wolRewardPlayer.getPlayer().getWol());
                assets.addAll(wolRewardPlayer.getAssets());
            });
            wolRewardCheckStockReq.setAssets(assets);
            wolRewardCheckStockReq.setKen(totalKenRewards.get());
            wolRewardCheckStockReq.setWol(totalWolRewards.get());
            wolRewardCheckStockReq.setReward_id("reward_id_of_game_server_" + System.currentTimeMillis());

            JSONObject jsonObject = this.checkStockReward(wolRewardCheckStockReq, checkStock);
            if (jsonObject == null) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveRewardCompletedRequest(WolRewardCheckStockRes wolRewardCheckStockRes, List<WolRewardPlayer> wolRewardPlayers) {
        WolRewardCompleteReq wolRewardCompleteReq = new WolRewardCompleteReq();
        wolRewardCompleteReq.setReward_id(wolRewardCheckStockRes.getReward_id());
        wolRewardCompleteReq.setPlayers(wolRewardPlayers);
        wolRewardCheckStockRess.put(wolRewardCompleteReq.getReward_id(), wolRewardCompleteReq);
    }

    public WolRewardCompleteRes rewardFlowComplete(String rewardId) {
        try {
            WolRewardCompleteReq wolRewardCheckStocReq = wolRewardCheckStockRess.get(rewardId);
            WolRewardCompleteRes wolAssetCompletedRes = this.rewardCompleted(wolRewardCheckStocReq);
            wolRewardCheckStockRess.remove(rewardId);
            return wolAssetCompletedRes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public WolAssetCompletedRes assetFlowComplete(WolAssetCompletedReq assetCompletedReq) {
        try {
            JSONObject jsonObject = this.assetCompleted(assetCompletedReq);
            return new WolAssetCompletedRes().init(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void upgradeFlow() {
        try {
            WolPlayerRes x000001 = this.getInfo("x000001");
            WolAssetUpgradeReq wolAssetUpgradeReq = new WolAssetUpgradeReq();
            ArrayList<WolAsset> assetsIn = new ArrayList<>();
            ArrayList<WolAsset> assetsOut = new ArrayList<>();
            WolAsset e = new WolAsset();
            e.setAsset_id("test_id_of_game");
            e.setStatus("in_game");
            e.setCategory("hero");
            e.setType("dark_knight");
            e.setAclass("b_rank");
            e.setGame(GAME_KEY);
            WolAsset b = new WolAsset();
            b.setAsset_id("test_id_of_game");
            b.setStatus("in_game");
            b.setCategory("hero");
            b.setType("dark_knight");
            b.setAclass("a_rank");
            b.setGame(GAME_KEY);
            assetsIn.add(e);
            assetsOut.add(b);
            wolAssetUpgradeReq.setInputs(assetsIn);
            wolAssetUpgradeReq.setOutputs(assetsOut);
            wolAssetUpgradeReq.setPlayer(x000001);


            JSONObject jsonObject = this.assetUpgrade(wolAssetUpgradeReq, new OffChainResponseHandler() {
                @Override
                public JSONObject onOk(JSONObject jsonObject) {
                    return null;
                }

                @Override
                public JSONObject onNg(JSONObject jsonObject) {
                    return null;
                }
            });

            WolAssetUpgradeRes wolAssetUpgradeRes = new WolAssetUpgradeRes().init(jsonObject);

            WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
            wolAssetCompletedReq.setAssets(wolAssetUpgradeRes.getAssets());
            wolAssetCompletedReq.setPlayer(wolAssetUpgradeRes.getPlayer());

            this.assetCompleted(wolAssetCompletedReq);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public String getGAME_KEY() {
        return SysConfig.getInstance().getKeyCfg("offchain.game", GAME_KEY);
    }

    public WolAssetCompletedRes upgradeCompleted(WolAssetCompletedReq wolAssetCompletedReq) {
        return assetFlowComplete(wolAssetCompletedReq);
    }

    public void buildWolAssets(String rewards, ArrayList<WolAsset> assets, AtomicInteger totalWol, AtomicInteger totalKen) {
        Collection<HeroItem> heroItems = ItemConfig.getInstance().splitItemToHeroItem(rewards);
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroConsumeItem) {
                switch (heroItem.getIndex()) {
                    case KEN:
                        totalKen.addAndGet(heroItem.getNo());
                        break;
                    case WOL:
                        totalWol.addAndGet(heroItem.getNo());
                        break;
                    case EGG:
                    case STARTER:
                        WolAsset a = new WolAsset();
                        a.setAsset_id(heroItem.getIndex());
                        a.setStatus("in_game");
                        a.setCategory("box");
                        if (heroItem.getIndex().equals(EGG))
                            a.setType("egg");
                        else if (heroItem.getIndex().equals(STARTER))
                            a.setType("starter");

                        a.setGame(OffChainServices.getInstance().getGAME_KEY());
                        a.setOfcId(heroItem.getOfcId());
                        buildAttribute(a, heroItem);
                        assets.add(a);
                        break;
                }
            } else if (heroItem instanceof HeroEquipment) {
                heroItem.initItemBase();
                WolAsset a = new WolAsset();
                a.setAsset_id(heroItem.getIndex());
                a.setStatus("in_game");
                a.setCategory("equipment");
                a.setType(Utils.getOTypeItem(((EquipBase) heroItem.getItemBase()).getSlot()));
                a.setAclass(Utils.getOClassEquip(heroItem.getRank()));
                a.setGame(OffChainServices.getInstance().getGAME_KEY());
                buildAttribute(a, heroItem);
                assets.add(a);
            }
        });
    }

    public void buildWolAssets(String rewards, AtomicInteger totalWol, AtomicInteger totalKen) {
        Collection<HeroItem> heroItems = ItemConfig.getInstance().splitItemToHeroItem(rewards);
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroConsumeItem) {
                switch (heroItem.getIndex()) {
                    case KEN:
                        totalKen.addAndGet(heroItem.getNo());
                        break;
                    case WOL:
                        totalWol.addAndGet(heroItem.getNo());
                        break;
                }
            }
        });
    }

    public void buildHeroAssets(List<HeroClass> rewards, ArrayList<WolAsset> assets) {
        rewards.forEach(sRewardBase -> {
            HeroBase heroBase = HeroConfig.getInstance().getHeroBase(sRewardBase.getCharIndex());
            WolAsset e = new WolAsset();
            e.setAsset_id(heroBase.getID());
            e.setStatus("in_game");
            e.setCategory("hero");
            e.setType(Utils.getOTypeHero(heroBase.getHeroClass()));
            e.setAclass(Utils.getOClassHero(heroBase.getRarity()));
            e.setGame(OffChainServices.getInstance().getGAME_KEY());
            e.setOfcId(sRewardBase.getOfcId());
            buildAttribute(e, sRewardBase);
            assets.add(e);
        });
    }

    private void buildAttribute(WolAsset e, HeroClass sRewardBase) {
        e.getAttribute().put("level", sRewardBase.getLevel());
        e.getAttribute().put("rank", sRewardBase.getRank());
        HeroBase heroBase = HeroConfig.getInstance().getHeroBase(sRewardBase.getCharIndex());
        e.copyGameDTO(heroBase.toGameAsset());
    }

    public void buildAttribute(WolAsset e, HeroBase heroBase) {
        e.getAttribute().put("level", 1);
        e.getAttribute().put("rank", 1);
        e.copyGameDTO(heroBase.toGameAsset());
    }

    private void buildAttribute(WolAsset e, HeroItem sRewardBase) {
        e.getAttribute().put("level", sRewardBase.getLevel());
        e.getAttribute().put("rank", sRewardBase.getRank());
        ItemBase itemBase = ItemConfig.getInstance().getItem(sRewardBase.getIndex());
        e.copyGameDTO(itemBase.toGameAsset());
        if (itemBase instanceof EquipBase) {
            e.getAttribute().put("image_url", ((EquipBase) itemBase).getRanks().get(sRewardBase.getRank()).getIcon());
        }
    }

    public void buildAttribute(WolAsset e, RewardBase sRewardBase) {
        e.getAttribute().put("level", sRewardBase.getLevel());
        e.getAttribute().put("rank", sRewardBase.getRank());
        HeroBase heroBase = HeroConfig.getInstance().getHeroBase(sRewardBase.getID());
        e.copyGameDTO(heroBase.toGameAsset());
    }

    public void buildItemAsset(Collection<HeroItem> heroItems, ArrayList<WolAsset> assets, AtomicInteger totalWol, AtomicInteger totalKen) {
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroConsumeItem) {
                switch (heroItem.getIndex()) {
                    case KEN:
                        totalKen.addAndGet(heroItem.getNo());
                        break;
                    case WOL:
                        totalWol.addAndGet(heroItem.getNo());
                        break;
                    case EGG:
                    case STARTER:
                        WolAsset a = new WolAsset();
                        a.setAsset_id(heroItem.getIndex());
                        a.setStatus("in_game");
                        a.setCategory("box");
                        if (heroItem.getIndex().equals(EGG))
                            a.setType("egg");
                        else if (heroItem.getIndex().equals(STARTER))
                            a.setType("starter");

                        a.setGame(OffChainServices.getInstance().getGAME_KEY());
                        a.setOfcId(heroItem.getOfcId());
                        buildAttribute(a, heroItem);
                        assets.add(a);
                        break;
                }
            } else if (heroItem instanceof HeroEquipment) {
                heroItem.initItemBase();
                WolAsset a = new WolAsset();
                a.setAsset_id(heroItem.getIndex());
                a.setStatus("in_game");
                a.setCategory("equipment");
                a.setType(Utils.getOTypeItem(((EquipBase) heroItem.getItemBase()).getSlot()));
                a.setAclass(Utils.getOClassEquip(heroItem.getRank()));
                a.setGame(OffChainServices.getInstance().getGAME_KEY());
                buildAttribute(a, heroItem);
                assets.add(a);
            }
        });
    }

    public void buildWolAssets(List<RewardBase> rewards, ArrayList<WolAsset> assets, AtomicInteger totalWol, AtomicInteger totalKen) {
        Map<String, Integer> refundFinal = new ConcurrentHashMap<>();
        rewards.forEach(sRewardBase -> {
            if (sRewardBase.getType().equals("hero")) {
                HeroBase heroBase = HeroConfig.getInstance().getHeroBase(sRewardBase.getID());
                WolAsset e = new WolAsset();
                e.setAsset_id(sRewardBase.getID());
                e.setStatus("in_game");
                e.setCategory("hero");
                e.setType(Utils.getOTypeHero(heroBase.getHeroClass()));
                e.setAclass(Utils.getOClassHero(heroBase.getRarity()));
                e.setGame(OffChainServices.getInstance().getGAME_KEY());
                buildAttribute(e, sRewardBase);
                assets.add(e);
            } else {
                refundFinal.put(sRewardBase.getID(), sRewardBase.getCount());
            }
        });

        Collection<HeroItem> heroItems = ItemConfig.getInstance().convertToHeroItem(refundFinal);
        heroItems.forEach(heroItem -> {
            if (heroItem instanceof HeroConsumeItem) {
                switch (heroItem.getIndex()) {
                    case KEN:
                        totalKen.addAndGet(heroItem.getNo());
                        break;
                    case WOL:
                        totalWol.addAndGet(heroItem.getNo());
                        break;
                    case EGG:
                    case STARTER:
                        WolAsset a = new WolAsset();
                        a.setAsset_id(heroItem.getIndex());
                        a.setStatus("in_game");
                        a.setCategory("box");
                        if (heroItem.getIndex().equals(EGG))
                            a.setType("egg");
                        else if (heroItem.getIndex().equals(STARTER))
                            a.setType("starter");

                        a.setGame(OffChainServices.getInstance().getGAME_KEY());
                        a.setOfcId(heroItem.getOfcId());
                        buildAttribute(a, heroItem);
                        assets.add(a);
                        break;
                }
            } else if (heroItem instanceof HeroEquipment) {
                heroItem.initItemBase();
                WolAsset a = new WolAsset();
                a.setAsset_id(heroItem.getIndex());
                a.setStatus("in_game");
                a.setCategory("equipment");
                a.setType(Utils.getOTypeItem(((EquipBase) heroItem.getItemBase()).getSlot()));
                a.setAclass(Utils.getOClassEquip(heroItem.getRank()));
                a.setGame(OffChainServices.getInstance().getGAME_KEY());
                buildAttribute(a, heroItem);
                assets.add(a);
            }
        });
    }

    public void applyOfcToItem(String playerID, Collection<HeroItem> heroItems, WolRewardCompleteRes wolRewardCompleteRes) {
        PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
        Player pLayer = playerManager.getPlayer(playerID);
        Optional<WolRewardPlayer> first = wolRewardCompleteRes.getPlayers().stream().filter(wolRewardPlayer -> wolRewardPlayer.getPlayer().getAddress().equals(pLayer.getWalletAddress())).findFirst();
        if (first.isPresent()) {
            WolRewardPlayer wolRewardPlayer = first.get();
            List<WolAsset> assets = wolRewardPlayer.getAssets();
            assets.forEach(wolAsset -> {
                Optional<HeroItem> first1 = heroItems.stream().filter(heroItem -> heroItem.getIndex().equals(wolAsset.getAsset_id()) && Utils.isNullOrEmpty(heroItem.getOfcId())).findFirst();
                first1.ifPresent(heroItem -> heroItem.setOfcId(wolAsset.getOfcId()));
            });
        }
    }

    public void applyOfcToItem(Collection<HeroItem> heroItems, WolAssetCompletedRes wolAssetCompletedRes) {
        List<WolAsset> assets = wolAssetCompletedRes.getAssets();
        assets.forEach(wolAsset -> {
            Optional<HeroItem> first1 = heroItems.stream().filter(heroItem -> heroItem.getIndex().equals(wolAsset.getAsset_id()) && Utils.isNullOrEmpty(heroItem.getOfcId())).findFirst();
            first1.ifPresent(heroItem -> heroItem.setOfcId(wolAsset.getOfcId()));
        });
    }

    public void applyOfcToHeroes(String playerID, List<HeroClass> heroes, WolRewardCompleteRes wolRewardCompleteRes) {
        PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
        Player pLayer = playerManager.getPlayer(playerID);
        Optional<WolRewardPlayer> first = wolRewardCompleteRes.getPlayers().stream().filter(wolRewardPlayer -> wolRewardPlayer.getPlayer().getAddress().equals(pLayer.getWalletAddress())).findFirst();
        if (first.isPresent()) {
            WolRewardPlayer wolRewardPlayer = first.get();
            List<WolAsset> assets = wolRewardPlayer.getAssets();
            assets.forEach(wolAsset -> {
                Optional<HeroClass> first1 = heroes.stream().filter(heroClass -> heroClass.getCharIndex().equals(wolAsset.getAsset_id()) && Utils.isNullOrEmpty(heroClass.getOfcId())).findFirst();
                first1.ifPresent(heroItem -> heroItem.setOfcId(wolAsset.getOfcId()));
            });
        }
    }

    public void applyOfcToHeroes(List<HeroClass> heroes, WolAssetCompletedRes wolAssetCompletedRes) {
        List<WolAsset> assets = wolAssetCompletedRes.getAssets();
        assets.forEach(wolAsset -> {
            Optional<HeroClass> first1 = heroes.stream().filter(heroClass -> heroClass.getCharIndex().equals(wolAsset.getAsset_id()) && Utils.isNullOrEmpty(heroClass.getOfcId())).findFirst();
            first1.ifPresent(heroItem -> heroItem.setOfcId(wolAsset.getOfcId()));
        });
    }

    public void applyOfcToHero(HeroClass heroClass, WolAssetCompletedRes wolAssetCompletedRes) {
        List<WolAsset> assets = wolAssetCompletedRes.getAssets();
        Optional<WolAsset> first1 = assets.stream().filter(asset -> asset.getAsset_id().equals(heroClass.getCharIndex())).findFirst();
        first1.ifPresent(wolAsset -> heroClass.setOfcId(wolAsset.getOfcId()));
    }
}
