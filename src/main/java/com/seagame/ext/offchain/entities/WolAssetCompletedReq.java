package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WolAssetCompletedReq extends WolReq {
    List<WolAsset> assets;
    WolPlayerRes player;
}
