package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WolRewardCompleteReq extends WolReq {
    List<WolRewardPlayer> players;
    String reward_id;
}
