package com.seagame.ext.offchain.entities;

import java.util.List;

public class WolRewardCheckStockReq extends WolReq {
    List<WolAsset> assets;
    String reward_id;
    int wol;
    int ken;
}
