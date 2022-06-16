package com.seagame.ext.offchain.entities;

import lombok.Getter;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WolRewardCompleteRes {
    List<WolRewardPlayer> players;
    String reward_id;

    public WolRewardCompleteRes init(JSONObject info) {
        this.reward_id = info.getString("reward_id");
        this.players = new ArrayList<>();
        info.getJSONArray("players").forEach(o -> this.players.add(new WolRewardPlayer().init((JSONObject) o)));
        return this;
    }
}
