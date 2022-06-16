package com.seagame.ext.offchain;

import com.seagame.ext.offchain.entities.WolAssetCompletedRes;
import net.sf.json.JSONObject;

public interface IApplyUpgrade {
    void applyUpgrade(boolean success, WolAssetCompletedRes assetCompletedRes, JSONObject jsonObject);
}
