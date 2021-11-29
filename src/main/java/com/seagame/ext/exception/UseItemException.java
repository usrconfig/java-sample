package com.seagame.ext.exception;

/**
 * @author LamHM
 */
public class UseItemException extends Exception {
    private static final long serialVersionUID = 1L;
    public static final int LACK_OF_ITEM = 1000;
    private Integer itemIndex;


    public UseItemException() {
    }


    public UseItemException(Integer itemIndex) {
        this.itemIndex = itemIndex;
    }


    public Integer getItemIndex() {
        return itemIndex;
    }


    public static UseItemException lackOfItem() {
        return new UseItemException(LACK_OF_ITEM);
    }
}
