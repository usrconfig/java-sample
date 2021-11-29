package com.seagame.ext.entities.item;

import com.creants.creants_2x.core.util.QAntTracer;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.protocol.serialization.SerializableQAntType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.seagame.ext.exception.GameException;
import com.seagame.ext.util.NetworkConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author LamHM
 */
@Getter
@Setter
@ToString
@Document(collection = "items")
public abstract class HeroItem implements SerializableQAntType, NetworkConstant {
    public @Id
    long id;
    public @Indexed
    String index;
    private @Indexed
    String playerId;
    private @Indexed
    long heroId = 0;
    int level = 1;
    int rank = 1;
    String type;
    int cofferState;
    public int no;
    int equipSlot;
    boolean lock;

    private String nftToken = "tokenNFT";

    @Transient
    private ItemBase itemBase;
    private long sellTime;

    public HeroItem() {
    }

    public HeroItem(ItemBase itemBase) {
        setItemBase(itemBase);
        setIndex(itemBase.getId());
        setType(itemBase.getType());
        setRank(Math.max(itemBase.getRank(), rank));
    }

    public long decr(int value) {
        if (value < 0) {
            QAntTracer.logPayment(this.getClass(), QAntTracer.getTraceMessage(new GameException("Item Desc " + playerId + "/" + value + "/" + no)));
            return -1;
        }
        no -= value;
        return no;
    }

    public HeroItem incr(int value) {
        if (value < 0) {
            QAntTracer.logPayment(this.getClass(), QAntTracer.getTraceMessage(new GameException("Item Desc " + playerId + "/" + value + "/" + no)));
            return this;
        }
        this.no += value;
        return this;
    }

    public void putInCoffer(boolean isPutIn) {
        this.cofferState = isPutIn ? 1 : 0;
    }

    public boolean isCurrencyItem() {
        return type != null && type.equals("currency");
    }

    public void setItemBase(ItemBase itemBase) {
        if (itemBase == null) {
            QAntTracer.error(this.getClass(), "[ERROR] item not found: " + getIndex());
            return;
        }
        this.itemBase = itemBase;
    }

    public boolean isOverlap() {
        return true;
    }


    @JsonIgnore
    public boolean canBeRemoved() {
        return !isCurrencyItem() && no <= 0;
    }


    @JsonIgnore
    public boolean canNotRemove() {
        return !canBeRemoved();
    }


    public abstract IQAntObject buildInfo();

    public abstract IQAntObject buildShortInfo();


    public abstract int getPower();

    public String getSellPrice() {
        return "29/" + (10 * level * itemBase.getRank());
    }

    public String getBuyPrice() {
        return "29/" + (10 * level * itemBase.getRank() * 3);
    }
}
