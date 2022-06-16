package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class WolPlayerRes {
    String address;
    int ken;
    int wol;
    List<WolAsset> assets;


    public WolPlayerRes init(JSONObject info) {
        this.address = info.getString("address");
        this.wol = info.getInt("wol");
        this.ken = info.getInt("ken");
        this.assets = new ArrayList<>();
        if (info.containsKey("assets")) {
            info.getJSONArray("assets").forEach(o -> this.assets.add(new WolAsset().init((JSONObject) o)));
        }
        return this;
    }
}
