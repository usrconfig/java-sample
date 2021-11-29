package com.seagame.ext.entities.item;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;

/**
 * @author LamHM
 */
public class ShortItemExt implements SerializableQAntType {
    public String index;
    public int no;


    public String getIndex() {
        return index;
    }


    public void setIndex(String index) {
        this.index = index;
    }


    public int getNo() {
        return no;
    }


    public void setNo(int no) {
        this.no = no;
    }


    @Override
    public String toString() {
        return "{index: " + index + ", no:" + no + "}";
    }

}
