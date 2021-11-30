package com.seagame.ext.entities;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.seagame.ext.util.NetworkConstant;

/**
 * @author LamHM
 */
public class GroupCount implements SerializableQAntType, NetworkConstant {
    private String group;
    private int no;


    public GroupCount() {
    }


    public GroupCount(String group, int count) {
        this.group = group;
        this.no = count;
    }


    public String getGroup() {
        return group;
    }


    public void setGroup(String group) {
        this.group = group;
    }


    public int getCount() {
        return no;
    }


    public void setCount(int count) {
        this.no = count;
    }


    public void incrValue(int value) {
        this.no += value;
    }


    public boolean isNewFriendGroup() {
        return group.equals(NEW_FRIEND_GROUP);
    }


    public void acceptNewFriend() {
        if (isNewFriendGroup())
            setCount(1);
        else
            incrValue(-1);
    }


    public boolean isRequestGroup() {
        return group.equals(FRIEND_REQUEST_GROUP);
    }

}
