package com.seagame.ext.offchain.services;

import com.seagame.ext.Utils;
import com.seagame.ext.config.game.HeroConfig;
import com.seagame.ext.entities.Player;
import com.seagame.ext.entities.hero.HeroBase;
import com.seagame.ext.entities.item.HeroItem;
import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.managers.PlayerManager;
import com.seagame.ext.offchain.*;
import com.seagame.ext.offchain.entities.*;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Service
public class WolFlowManager implements InitializingBean {


    @Autowired
    PlayerManager playerManager;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public void sendRewardRequest(String playerId, IGenReward iGenReward, IApplyRewards iApplyRewards) {
        sendRewardRequest(playerId, iGenReward, iApplyRewards, 50);
    }

    public void sendAssetRequest(String playerId, IGenReward iGenReward, IApplyAssets iApplyRewards, int retry) {
        retry = Math.max(0, Math.min(retry, 50));
        Player player = playerManager.getPlayer(playerId);
        List<WolRewardPlayer> wolRewardPlayers = new ArrayList<>();
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> rewards = new AtomicReference<>();
        OffChainResponseHandler checkStock = new OffChainResponseHandler() {
            @Override
            public JSONObject onOk(JSONObject jsonObject) {
                WolAssetCheckStockRes wolAssetCheckStockRes = new WolAssetCheckStockRes().init(jsonObject);
                WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
                wolAssetCompletedReq.setPlayer(wolAssetCheckStockRes.getPlayer());
                wolAssetCompletedReq.setAssets(wolAssetCheckStockRes.getAssets());
                WolAssetCompletedRes wolRewardCompleteRes = OffChainServices.getInstance().assetFlowComplete(wolAssetCompletedReq);
                iApplyRewards.applyAssets(rewards.get(), wolRewardCompleteRes);
                success.set(true);
                return jsonObject;
            }

            @Override
            public JSONObject onNg(JSONObject jsonObject) {
                return null;
            }
        };
        IntStream.range(1, retry).forEach(value -> {
            if (success.get()) {
                return;
            }

            ArrayList<WolAsset> assets = new ArrayList<>();

            String s = iGenReward.genRewards();
            if (s != null) {
                HeroBase heroBase = HeroConfig.getInstance().getHeroBase(s);
                rewards.set(s);
                WolAsset e = new WolAsset();
                e.setAsset_id(heroBase.getID());
                e.setStatus("in_game");
                e.setCategory("hero");
                e.setType(Utils.getOTypeHero(heroBase.getHeroClass()));
                e.setAclass(Utils.getOClassHero(heroBase.getRarity()));
                e.setGame(OffChainServices.getInstance().getGAME_KEY());
                OffChainServices.getInstance().buildAttribute(e, heroBase);
                assets.add(e);
            }
            OffChainServices.getInstance().assetFlow(player.getWalletAddress(), assets, checkStock);
        });
    }

    public void sendRewardRequest(String playerId, IGenReward iGenReward, IApplyRewards iApplyRewards, int retry) {
        retry = Math.max(0, Math.min(retry, 50));
        Player player = playerManager.getPlayer(playerId);
        List<WolRewardPlayer> wolRewardPlayers = new ArrayList<>();
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> rewards = new AtomicReference<>();
        OffChainResponseHandler checkStock = new OffChainResponseHandler() {
            @Override
            public JSONObject onOk(JSONObject jsonObject) {
                WolRewardCheckStockRes wolAssetCheckStockRes = new WolRewardCheckStockRes().init(jsonObject);
                OffChainServices.getInstance().saveRewardCompletedRequest(wolAssetCheckStockRes, wolRewardPlayers);
                WolRewardCompleteRes wolRewardCompleteRes = OffChainServices.getInstance().rewardFlowComplete(wolAssetCheckStockRes.getReward_id());
                iApplyRewards.applyRewards(rewards.get(), wolRewardCompleteRes);
                success.set(true);
                return jsonObject;
            }

            @Override
            public JSONObject onNg(JSONObject jsonObject) {
                return null;
            }
        };
        IntStream.range(1, retry).forEach(value -> {
            if (success.get()) {
                return;
            }
            wolRewardPlayers.clear();
            AtomicInteger totalWol = new AtomicInteger();
            AtomicInteger totalKen = new AtomicInteger();
            ArrayList<WolAsset> assets = new ArrayList<>();

            if (iGenReward.genRewards() != null) {
                rewards.set(iGenReward.genRewards());
                OffChainServices.getInstance().buildWolAssets(rewards.get(), assets, totalWol, totalKen);
            } else {
                OffChainServices.getInstance().buildWolAssets(iGenReward.genRewardsBase(), assets, totalWol, totalKen);
            }

            WolRewardPlayer rewardPlayer = new WolRewardPlayer();
            WolPlayerRes wolplayer = new WolPlayerRes();
            wolplayer.setWol(totalWol.get());
            wolplayer.setKen(totalKen.get());
            wolplayer.setAddress(player.getWalletAddress());
            rewardPlayer.setPlayer(wolplayer);
            rewardPlayer.setAssets(assets);
            wolRewardPlayers.add(rewardPlayer);
            OffChainServices.getInstance().rewardFlowCheckStock(wolRewardPlayers, checkStock);
        });
    }

    public void sendUpgradeRequest(String playerId, ArrayList<WolAsset> assetsIn, ArrayList<WolAsset> assetsOut, IApplyUpgrade iApplyUpgrade, int wol, int ken) {
        try {
            Player player = playerManager.getPlayer(playerId);
            WolPlayerRes x000001 = OffChainServices.getInstance().getInfo(player.getWalletAddress());
            WolAssetUpgradeReq wolAssetUpgradeReq = new WolAssetUpgradeReq();
            wolAssetUpgradeReq.setInputs(assetsIn);
            wolAssetUpgradeReq.setOutputs(assetsOut);
            wolAssetUpgradeReq.setPlayer(x000001);
            wolAssetUpgradeReq.setWol(wol);
            wolAssetUpgradeReq.setKen(ken);

            OffChainResponseHandler assetUpgrade = new OffChainResponseHandler() {
                @Override
                public JSONObject onOk(JSONObject jsonObject) {
                    WolAssetUpgradeRes wolAssetUpgradeRes = new WolAssetUpgradeRes().init(jsonObject);
                    WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
                    wolAssetCompletedReq.setAssets(wolAssetUpgradeRes.getAssets());
                    wolAssetCompletedReq.setPlayer(wolAssetUpgradeRes.getPlayer());
                    WolAssetCompletedRes wolAssetCompletedRes = OffChainServices.getInstance().upgradeCompleted(wolAssetCompletedReq);
                    iApplyUpgrade.applyUpgrade(true, wolAssetCompletedRes, jsonObject);
                    return jsonObject;
                }

                @Override
                public JSONObject onNg(JSONObject jsonObject) {
                    iApplyUpgrade.applyUpgrade(false, null, jsonObject);
                    return jsonObject;
                }
            };
            OffChainServices.getInstance().assetUpgrade(wolAssetUpgradeReq, assetUpgrade);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendUseItemRequest(String playerId, ArrayList<WolAsset> assetsIn, ArrayList<WolAsset> assetsOut, IApplyUseItemRewards iApplyUseItemRewards, int wol, int ken, String rewards, AtomicBoolean success) {
        try {
            Player player = playerManager.getPlayer(playerId);
            WolPlayerRes x000001 = OffChainServices.getInstance().getInfo(player.getWalletAddress());
            WolAssetUpgradeReq wolAssetUpgradeReq = new WolAssetUpgradeReq();
            wolAssetUpgradeReq.setInputs(assetsIn);
            wolAssetUpgradeReq.setOutputs(assetsOut);
            wolAssetUpgradeReq.setPlayer(x000001);
            wolAssetUpgradeReq.setWol(wol);
            wolAssetUpgradeReq.setKen(ken);

            OffChainResponseHandler assetUpgrade = new OffChainResponseHandler() {
                @Override
                public JSONObject onOk(JSONObject jsonObject) {
                    WolAssetUpgradeRes wolAssetUpgradeRes = new WolAssetUpgradeRes().init(jsonObject);
                    WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
                    wolAssetCompletedReq.setAssets(wolAssetUpgradeRes.getAssets());
                    wolAssetCompletedReq.setPlayer(wolAssetUpgradeRes.getPlayer());
                    WolAssetCompletedRes wolAssetCompletedRes = OffChainServices.getInstance().upgradeCompleted(wolAssetCompletedReq);
                    iApplyUseItemRewards.applyRewards(rewards, wolAssetCompletedRes, jsonObject);
                    success.set(true);
                    return jsonObject;
                }

                @Override
                public JSONObject onNg(JSONObject jsonObject) {
//                    iApplyUseItemRewards.applyRewards(rewards.get(), null, jsonObject);
                    return jsonObject;
                }
            };
            OffChainServices.getInstance().assetUpgrade(wolAssetUpgradeReq, assetUpgrade);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendUseItemRequest(String playerId, ArrayList<WolAsset> assetsIn, ArrayList<WolAsset> assetsOut, IApplyUseItemRewards iApplyUseItemRewards, int wol, int ken, List<RewardBase> rewardBases, AtomicBoolean success) {
        try {
            Player player = playerManager.getPlayer(playerId);
            WolPlayerRes x000001 = OffChainServices.getInstance().getInfo(player.getWalletAddress());
            WolAssetUpgradeReq wolAssetUpgradeReq = new WolAssetUpgradeReq();
            wolAssetUpgradeReq.setInputs(assetsIn);
            wolAssetUpgradeReq.setOutputs(assetsOut);
            wolAssetUpgradeReq.setPlayer(x000001);
            wolAssetUpgradeReq.setWol(wol);
            wolAssetUpgradeReq.setKen(ken);

            OffChainResponseHandler assetUpgrade = new OffChainResponseHandler() {
                @Override
                public JSONObject onOk(JSONObject jsonObject) {
                    WolAssetUpgradeRes wolAssetUpgradeRes = new WolAssetUpgradeRes().init(jsonObject);
                    WolAssetCompletedReq wolAssetCompletedReq = new WolAssetCompletedReq();
                    wolAssetCompletedReq.setAssets(wolAssetUpgradeRes.getAssets());
                    wolAssetCompletedReq.setPlayer(wolAssetUpgradeRes.getPlayer());
                    WolAssetCompletedRes wolAssetCompletedRes = OffChainServices.getInstance().upgradeCompleted(wolAssetCompletedReq);
                    iApplyUseItemRewards.applyRewards(rewardBases, wolAssetCompletedRes, jsonObject);
                    success.set(true);
                    return jsonObject;
                }

                @Override
                public JSONObject onNg(JSONObject jsonObject) {
//                    iApplyUseItemRewards.applyRewards(rewards.get(), null, jsonObject);
                    return jsonObject;
                }
            };
            OffChainServices.getInstance().assetUpgrade(wolAssetUpgradeReq, assetUpgrade);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendUseItemRequest(String playerId, Collection<HeroItem> collection, IGenReward iGenReward, IApplyUseItemRewards iApplyUseItemRewards) {
        AtomicBoolean success = new AtomicBoolean(false);
        IntStream.range(1, 50).forEach(value -> {
            if (success.get()) {
                return;
            }
            ArrayList<WolAsset> assetIn = new ArrayList<>();
            ArrayList<WolAsset> assetOut = new ArrayList<>();
            AtomicInteger totalWol = new AtomicInteger();
            AtomicInteger totalKen = new AtomicInteger();
            OffChainServices.getInstance().buildItemAsset(collection, assetIn, totalWol, totalKen);

            if (iGenReward.genRewards() != null) {
                String rewards = iGenReward.genRewards();
                OffChainServices.getInstance().buildWolAssets(rewards, assetOut, totalWol, totalKen);
                sendUseItemRequest(playerId, assetIn, assetOut, iApplyUseItemRewards, totalWol.get(), totalKen.get(), rewards, success);
            } else {
                List<RewardBase> rewards1 = iGenReward.genRewardsBase();
                OffChainServices.getInstance().buildWolAssets(rewards1, assetOut, totalWol, totalKen);
                sendUseItemRequest(playerId, assetIn, assetOut, iApplyUseItemRewards, totalWol.get(), totalKen.get(), rewards1, success);
            }
        });
    }
}
