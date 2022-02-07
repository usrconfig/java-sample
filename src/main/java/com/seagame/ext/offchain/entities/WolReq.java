package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class WolReq {
    String api_key;
    String system;
    String game;
}
