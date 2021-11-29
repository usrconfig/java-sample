package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class TakeOffException extends Exception {
    private static final long serialVersionUID = 1L;
    private GameErrorCode errorCode;


    public TakeOffException(GameErrorCode errorCode) {
        this.errorCode = errorCode;
    }


    public GameErrorCode getErrorCode() {
        return errorCode;
    }

}
