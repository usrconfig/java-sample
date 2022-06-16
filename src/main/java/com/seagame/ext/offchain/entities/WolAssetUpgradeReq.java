package com.seagame.ext.offchain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WolAssetUpgradeReq extends WolReq {
    WolPlayerRes player;
    List<WolAsset> inputs;
    List<WolAsset> outputs;
    @JsonProperty("wol_amount")
    @JsonSerialize(using = ToStringSerializer.class)
    int wol;
    @JsonProperty("ken_amount")
    @JsonSerialize(using = ToStringSerializer.class)
    int ken;
}
