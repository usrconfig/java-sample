package com.seagame.ext.entities.item;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;

/**
 * @author LamHM
 */
public class HeroConsumeItem extends HeroItem {

    @Override
    public IQAntObject buildInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putLong("id", id);
        result.putUtfString("idx", index);
        result.putBool("equip", isEquip());
        result.putInt("no", no);
        result.putInt("level", level != 0 ? level : 1);
        return result;
    }

    @Override
    public IQAntObject buildShortInfo() {
        return buildInfo();
    }

    public HeroConsumeItem(ItemBase itemBase) {
        super(itemBase);
    }

    public HeroConsumeItem() {
        super();
    }


    @Override
    public int getPower() {
        return 0;
    }

    @Override
    public boolean isEquip() {
        return false;
    }


}
