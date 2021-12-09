package com.seagame.ext.exception;

import com.creants.creants_2x.core.exception.IErrorCode;

/**
 * @author LamHM
 */
public enum GameErrorCode implements IErrorCode {
    NOT_ENOUGH_CURRENCY_ITEM("Not enough {0}", 0),
    LACK_OF_INFOMATION("LACK_OF_INFOMATION", 1),
    LACK_OF_MATIRIAL("LACK_OF_MATIRIAL", 2),


    NOT_EXIST_ITEM("NOT_EXIST_ITEM", 3),
    ITEM_CAN_NOT_SELL("ITEM_CAN_NOT_SELL", 4),
    ITEM_CAN_NOT_USE("ITEM_CAN_NOT_USE", 5),
    EXIST_HERO_NAME("EXIST_HERO_NAME", 6),
    NAME_MIN_MAX("NAME_MIN_MAX", 7),
    MAX_FRIEND("Maximum 25 friends", 8),
    ROOM_TIME_OUT("ROOM_TIME_OUT", 9),
    QUEST_IS_RECEIVED("QUEST_IS_RECEIVED", 10),
    GIFT_CODE_RECEIVED("GIFT_CODE_RECEIVED", 11),
    GIFT_CODE_NOT_FOUND("GIFT_CODE_NOT_FOUND", 12),
    MAIL_RECEIVED("MAIL_RECEIVED", 13),
    PLAYER_NOT_FOUND("PLAYER_NOT_FOUND", 14),
    NOT_ENOUGH_STAT_POINT("NOT_ENOUGH_STAT_POINT", 15),
    STAGE_NOT_FOUND("STAGE_NOT_FOUND", 16),
    NOT_ENOUGH_TICKET("NOT_ENOUGH_TICKET", 17),
    NOT_ENOUGH_ENERGY("NOT_ENOUGH_ENERGY", 18),


    //Hero 1xx
    RANK_IS_MAX("RANK_IS_MAX", 19),
    SKILL_NOT_FOUND("SKILL_NOT_FOUND", 20),
    SKILL_MAX_LEVEL("SKILL_MAX_LEVEL", 21),
    LEVEL_MAX_NEED_RANK_UP("LEVEL_MAX_NEED_RANK_UP", 22),

    //Campaign
    MATCH_NOT_FOUND("MATCH_NOT_FOUND", 24),


    LOGIN_BY_OTHER_DEIVCE("LOGIN_BY_OTHER_DEIVCE", 900),
    ACCOUNT_HAS_BEEN_LINKED("ACCOUNT_HAS_BEEN_LINKED", 901),
    UNKNOW_EXCEPTION("UNKNOW_EXCEPTION", 999);
    private short id;
    private String msg;


    private GameErrorCode(String msg, int id) {
        this.id = (short) id;
        this.msg = msg;
    }


    @Override
    public short getId() {
        return id;
    }


    public String getMsg() {
        return msg;
    }

}
