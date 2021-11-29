package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class NameException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final int UNKNOW_EXCEPTION = -1;
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 2;
    public static final int ALREADY = 3;
    private int code;
    private String msg;


    public NameException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public NameException(int code) {
        this.code = code;
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
