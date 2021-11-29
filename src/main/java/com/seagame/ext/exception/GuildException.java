package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class GuildException extends Exception {
    private static final long serialVersionUID = 1L;
    private GameErrorCode errorCode;


    public GuildException(GameErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    public GameErrorCode getErrorCode() {
        return errorCode;
    }


    public void setErrorCode(GameErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    @Override
    public String toString() {
        return "{code:" + errorCode.getId() + ", msg:" + errorCode.getMsg() + "}";
    }

}
