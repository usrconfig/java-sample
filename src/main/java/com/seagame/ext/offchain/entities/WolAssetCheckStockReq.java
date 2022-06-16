package com.seagame.ext.offchain.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WolAssetCheckStockReq extends WolReq {
    List<WolAsset> assets;
    WolPlayerRes player;
}
