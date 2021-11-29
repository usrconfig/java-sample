package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class PaymentException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final int UNKNOW_EXCEPTION = -1;
    public static final int CALL_INTERNAL_FAIL = 0;
    public static final int PROCESSED_PAYMENT = 200;
    private int code;
    private String msg;


    public PaymentException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }


    @Override
    public String toString() {
        return "{code:" + code + ", msg:" + msg + "}";
    }

}
