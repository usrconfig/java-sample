package com.seagame.ext.om;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author LamHM
 */
@Document(collection = "product-purchase")
public class ProductPurchase {
    private @Id
    long id;
    private @Indexed
    String gameHeroId;
    private @Indexed
    String productId;
    private @Indexed
    long creantsId;
    private int buyTimes;
    private Date purchaseTime;


    public IQAntObject buildObject() {
        IQAntObject obj = QAntObject.newInstance();
        obj.putUtfString("productId", productId);
        obj.putInt("buyTimes", buyTimes);
        return obj;
    }


    public long getCreantsId() {
        return creantsId;
    }


    public void setCreantsId(long creantsId) {
        this.creantsId = creantsId;
    }


    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public void incrBuyTimes() {
        buyTimes++;
    }


    public String getGameHeroId() {
        return gameHeroId;
    }


    public void setGameHeroId(String gameHeroId) {
        this.gameHeroId = gameHeroId;
    }


    public int getBuyTimes() {
        return buyTimes;
    }


    public void setBuyTimes(int buyTimes) {
        this.buyTimes = buyTimes;
    }


    public String getProductId() {
        return productId;
    }


    public void setProductId(String productId) {
        this.productId = productId;
    }


    public Date getPurchaseTime() {
        return purchaseTime;
    }


    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

}
