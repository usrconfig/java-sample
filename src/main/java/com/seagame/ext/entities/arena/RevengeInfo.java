package com.seagame.ext.entities.arena;

import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */
@Getter
@Setter
public class RevengeInfo {
    private ArenaPower opponent;
    private boolean isRevenged;
    private String historyId;

}
