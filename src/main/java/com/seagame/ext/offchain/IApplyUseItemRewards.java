package com.seagame.ext.offchain;

import com.seagame.ext.entities.item.RewardBase;
import com.seagame.ext.offchain.entities.WolAssetCompletedRes;
import net.sf.json.JSONObject;

import java.util.List;

public interface IApplyUseItemRewards {
    public void applyRewards(String rewards, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject);

    public void applyRewards(List<RewardBase> rewardBases, WolAssetCompletedRes wolAssetCompletedRes, JSONObject jsonObject);
}
