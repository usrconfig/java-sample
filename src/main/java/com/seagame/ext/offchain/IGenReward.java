package com.seagame.ext.offchain;

import com.seagame.ext.entities.item.RewardBase;

import java.util.List;

public interface IGenReward {
    public String genRewards();
    public List<RewardBase> genRewardsBase();
}
