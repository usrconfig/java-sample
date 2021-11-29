package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class OSException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final int LACK_OF_ITEM = 1000;
    private Integer itemIndex;


    public OSException() {
    }


    public OSException(Integer itemIndex) {
        this.itemIndex = itemIndex;
    }


    public Integer getItemIndex() {
        return itemIndex;
    }


    public static OSException lackOfItem() {
        return new OSException(LACK_OF_ITEM);
    }
}
