package com.seagame.ext.offchain.entities;

import com.creants.eventhandling.dto.GameAssetDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class WolAsset {
    @JsonProperty("_id")
    String ofcId;
    String asset_id;
    String status;
    String owner;
    String token_id;
    String category;
    String type;
    //    @JsonProperty("class")
    String aclass;//class
    Map<String, Object> attribute;
    int price;
    String game;


    public WolAsset() {
        this.attribute = new HashMap<>();
    }

    public WolAsset init(JSONObject o) {
        this.ofcId = o.containsKey("_id") ? o.getString("_id") : "";
        this.asset_id = o.getString("asset_id");
        this.status = o.getString("status");
        this.owner = o.containsKey("owner") ? o.getString("owner") : "";
        this.token_id = o.containsKey("token_id") ? o.getString("token_id") : "";
        this.category = o.getString("category");
        this.type = o.getString("type");
        this.aclass = o.containsKey("aclass") ? o.getString("aclass") : "b_rank";
        JSONObject attribute = o.getJSONObject("attribute");

        if (attribute != null) {
            attribute.put("name", attribute.containsKey("name") ? attribute.getString("name") : "");
            attribute.put("image_url", attribute.containsKey("image_url") ? attribute.getString("image_url") : "");
            attribute.put("description", attribute.containsKey("description") ? attribute.getString("description") : "");
        }

        this.price = o.containsKey("price") ? o.getInt("price") : 0;
        this.game = o.containsKey("game") ? o.getString("game") : "";
        return this;
    }

    public void copyGameDTO(GameAssetDTO toGameAsset) {
        this.attribute.putAll(toGameAsset.getAttribute());
    }
}
