package com.seagame.ext.om;

import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LamHM
 */
public class ItemPackageInfo implements SerializableQAntType {
    public int index;
    private transient String giftString;
    private transient String heroString;
    public List<ItemInfo> items;


    public ItemPackageInfo() {
        items = new ArrayList<>();
    }


    public int getIndex() {
        return index;
    }


    public void setIndex(int index) {
        this.index = index;
    }


    public String getGiftString() {
        return giftString;
    }


    public void setGiftString(String giftString) {
        this.giftString = giftString;
    }


    public List<ItemInfo> getItems() {
        return items;
    }


    public void setItems(List<ItemInfo> items) {
        this.items = items;
    }


    public void addItem(ItemInfo item) {
        items.add(item);
    }


    public String getHeroString() {
        return heroString;
    }


    public void setHeroString(String heroString) {
        this.heroString = heroString;
    }

}
