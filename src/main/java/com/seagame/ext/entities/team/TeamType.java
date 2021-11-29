package com.seagame.ext.entities.team;

import lombok.Getter;

/**
 * @author LamHM
 */
@Getter
public enum TeamType {
    CAMPAIGN("cp", "Campaign Team"),
    ARENA("ar", "Arena Team"),
    DEFENCE("df", "Defence Team");
    String code;
    String name;


    TeamType(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
