package com.seagame.ext.offchain;

import com.seagame.ext.offchain.entities.WolAssetCompletedRes;
import com.seagame.ext.offchain.entities.WolRewardCompleteRes;

public interface IApplyAssets {
    public void applyAssets(String rewards, WolAssetCompletedRes wolAssetCompletedRes);
}
