package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.apache.http.client.fluent.Form;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WolRewardCheckStockRes {
    List<WolAsset> assets;
    String reward_id;
    int wol;
    int ken;

    public WolRewardCheckStockRes init(JSONObject info) {
        this.reward_id = info.getString("reward_id");
        this.wol = info.getInt("wol");
        this.ken = info.getInt("ken");
        this.assets = new ArrayList<>();
        info.getJSONArray("assets").forEach(o -> this.assets.add(new WolAsset().init((JSONObject) o)));
        return this;
    }
}