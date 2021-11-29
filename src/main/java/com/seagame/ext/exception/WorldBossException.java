package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class WorldBossException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final int BOSS_DEATH = -1;
    public static final int NOT_ENOUGHT_TICKET = 0;
    private int code;
    private String msg;


    public WorldBossException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }


    public static WorldBossException bossDeath() {
        return new WorldBossException(BOSS_DEATH, "Boss death");
    }


    public static WorldBossException notEnoughTicket() {
        return new WorldBossException(NOT_ENOUGHT_TICKET, "NOT_ENOUGHT_TICKET");
    }


    public boolean isBossDeath() {
        return code == BOSS_DEATH;
    }


    public boolean isNotEnoughTicket() {
        return code == NOT_ENOUGHT_TICKET;
    }


    @Override
    public String toString() {
        return "{code:" + code + ", msg:" + msg + "}";
    }

}
