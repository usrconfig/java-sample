package com.seagame.ext.entities.item;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author LamHM
 */
@Getter
@Setter
public class HeroEquipment extends HeroItem implements SerializableQAntType {
    int reliability;

    public HeroEquipment() {
        super();
    }

    public HeroEquipment(ItemBase itemBase) {
        super(itemBase);
        this.reliability = 100;
        this.setType("equip");
    }

    public int reliabilityDesc(int value) {
        if (reliability > 0 && value > 0) {
            this.reliability = Math.max(0, reliability - value);
        }
        return this.reliability;
    }

    @Override
    public boolean isOverlap() {
        return false;
    }

    @Override
    public IQAntObject buildInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putLong("id", id);
        result.putUtfString("idx", index);
        result.putInt("level", level != 0 ? level : 1);
        result.putInt("rank", rank != 0 ? rank : 1);
        result.putInt("equipSlot", equipSlot);
        result.putInt("cofferState", cofferState);
        result.putUtfString("nftToken", getNftToken());
        return result;
    }

    public IQAntObject buildShortInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putLong("id", id);
        result.putUtfString("idx", index);
        result.putInt("no", no);
        result.putInt("equipSlot", equipSlot);
        return result;
    }


    @Override
    public int getPower() {
        return 0;
    }

    public void levelUp() {
        level += 1;
        levelUpCheck();
    }

    public void rankUp() {
        rank += 1;
        level = 1;
    }


    private void levelUpCheck() {
    }


    public void takeOn(int slot) {
        this.equipSlot = slot;
    }


    public void takeOff() {
        this.equipSlot = 0;
    }


    private void updateBaseStats() {
    }

    public void lockItem(Boolean isLock) {
        this.lock = isLock;
    }


}
