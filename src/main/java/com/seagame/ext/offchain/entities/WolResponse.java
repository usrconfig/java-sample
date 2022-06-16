package com.seagame.ext.offchain.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WolResponse {
    String status;
    String result;
    String errors;
    int num;
    int total_num;
}
