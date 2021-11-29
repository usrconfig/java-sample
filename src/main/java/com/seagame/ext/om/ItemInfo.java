package com.seagame.ext.om;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;

/**
 * @author LamHM
 */
public class ItemInfo implements SerializableQAntType {
    public String groupId;
    public String index;
    public int no;


    public ItemInfo(String groupId, String index, int no) {
        this.groupId = groupId;
        this.index = index;
        this.no = no;
    }


    public String getGroupId() {
        return groupId;
    }


    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


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

}
