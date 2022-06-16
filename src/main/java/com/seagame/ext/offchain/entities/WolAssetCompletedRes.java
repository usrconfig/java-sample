package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WolAssetCompletedRes {
    List<WolAsset> assets;

    public WolAssetCompletedRes init(JSONObject info) {
        this.assets = new ArrayList<>();
        if (info.containsKey("assets")) {
            info.getJSONArray("assets").forEach(o -> this.assets.add(new WolAsset().init((JSONObject) o)));
        }
        return this;
    }
}
