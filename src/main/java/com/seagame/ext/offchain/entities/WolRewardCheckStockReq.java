package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.fluent.Form;

import java.util.List;
@Setter
@Getter
public class WolRewardCheckStockReq extends WolReq {
    List<WolAsset> assets;
    String reward_id;
    int wol;
    int ken;
}
