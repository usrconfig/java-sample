package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WolAssetUpgradeRes {
    List<WolAsset> assets;
    WolPlayerRes player;

    public WolAssetUpgradeRes init(JSONObject info) {
        this.player = new WolPlayerRes().init(info.getJSONObject("player"));
        this.assets = new ArrayList<>();
        if (info.containsKey("assets")) {
            info.getJSONArray("assets").forEach(o -> this.assets.add(new WolAsset().init((JSONObject) o)));
        }
        return this;
    }
}
