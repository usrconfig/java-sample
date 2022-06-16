package com.seagame.ext.entities;

import com.creants.creants_2x.socket.gate.entities.IQAntObject;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.seagame.ext.config.game.GameConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.*;

/**
 * @author LamHM+
 */
@Document(collection = "players")
@Getter
@Setter
public class Player implements Serializable {
    private static final int[] SLOTS = new int[]{0, 1};

    public static final String WATCH_ADS = "watch_ads";
    public static final String FREE_CHEST = "free_chest";
    public static final String[] SPECIAL_ITEMS = new String[]{WATCH_ADS};

    static final long serialVersionUID = 1L;
    private static final int MAX_SLOT = 120;
    public static final String FB_PROVIDER = "fb";
    public static final String GG_PROVIDER = "gg";
    public static final String OS_PROVIDER = "os";
    private static final long ENERGY_PRODUCE_SPEED_SEC = 1800;
    private static final int MAX_ENERGY_PRODUCE = 5;

    @Id
    public String id;
    @Indexed
    public long gameId;
    @Indexed
    private long loginId;
    @Indexed
    private long login3rdId;
    @Indexed
    private String provider;
    @Indexed
    private String provider3rd;
    @Indexed
    private String deviceId;
    @Indexed
    private String walletAddress;
    @Indexed
    private String zoneName;
    private String avatar;
    public String name;
    public int level;
    public int exp;
    private int vipPoint;
    private int vipLevel;
    public boolean isNPC;
    private Date loginTime;
    private Date createTime;
    private Date logoutTime;
    private boolean isNewUser;
    private boolean named;
    private int dateCount;
    private int maxItemSlot;
    private int maxVaultItemSlot;
    private boolean rated;
    private String device;
    private @Transient
    boolean isOnline;
    private String deviceToken;
    private String deviceType;

    private int win;
    private int lose;
    @Indexed
    private int winRate;
    @Indexed
    private int kill;
    @Indexed
    private int maxHeroLevel;
    @Indexed
    private int trophy;

    public int rankRemain;
    public int freeChestDay;

    public int activeHeroId = 0;

    public int energyMax = 0;
    public int energy = 0;
    public long energyStartBuild = 0;


    public Player() {
        this.initSpecialItem();
    }

    private void initSpecialItem() {
        this.rankRemain = 10;
    }

    public Player(String id, long gameId, String fullName) {
        this();
        this.gameId = gameId;
        isNewUser = true;
        named = false;
        maxItemSlot = 60;
        maxVaultItemSlot = 0;
        this.id = id;
        name = fullName;
        vipLevel = 1;
        level = 1;
        maxHeroLevel = 1;
        avatar = getDefaultAvatar();
        Date date = new Date(System.currentTimeMillis());
        setLoginTime(date);
        setCreateTime(date);
        this.initSpecialItem();
    }

    private String getDefaultAvatar() {
        return "avatar_class_" + ((System.currentTimeMillis() % 5) + 1);
    }

    public IQAntObject buildFriendInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putUtfString("id", id);
        result.putUtfString("name", name);
        result.putUtfString("avatar", avatar != null ? avatar : getDefaultAvatar());
        result.putInt("level", level);
        result.putInt("trophy", trophy);
        result.putLong("now", isOnline() ? -1 : (getLogoutTime() == null ? -1 : getLogoutTime().getTime()));
        result.putBool("onBattle", isOnBattle());
        return result;
    }

    private boolean isOnBattle() {
        return false;
    }

    private void levelUp(int value) {
        level += value;
        levelupCheck();
    }

    public boolean expUp(int exp) {
        this.exp += exp;
        return levelupCheck();
    }

    private boolean levelupCheck() {
        int accLevelExp = GameConfig.getInstance().getAccLevelExp(level + 1);
        if (accLevelExp != -1 && exp >= accLevelExp) {
            levelUp(1);
            return true;
        }
        return false;
    }

    public IQAntObject buildLoginInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putUtfString("id", id);
        result.putUtfString("name", name);
        result.putLong("loginId", loginId);
        result.putLong("login3rdId", login3rdId);
        result.putUtfString("avatar", avatar != null ? avatar : getDefaultAvatar());
        result.putInt("level", level);
        result.putInt("exp", exp);
        result.putInt("dateCount", dateCount);
        result.putBool("named", named);
        result.putInt("trophy", trophy);
        result.putInt("rankRemain", rankRemain);
        result.putInt("energy", energy);
        result.putInt("energyMax", energyMax);
        result.putLong("energyStartBuild", energyStartBuild);
        return result;
    }

    public IQAntObject buildPointInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putUtfString("id", id);
        result.putInt("level", level);
        result.putInt("exp", exp);
        result.putInt("trophy", trophy);
        result.putInt("rankRemain", rankRemain);
        result.putInt("energy", energy);
        result.putInt("energyMax", energyMax);
        result.putLong("energyStartBuild", energyStartBuild);
        return result;
    }


    public IQAntObject buildLevelInfo() {
        IQAntObject result = QAntObject.newInstance();
        result.putUtfString("id", id);
        result.putInt("level", level);
        result.putInt("exp", exp);
        return result;
    }


    public boolean setLoginId(long loginId, String provider) {
        if (loginId > 0) {
            switch (provider) {
                case FB_PROVIDER:
                case GG_PROVIDER:
                case OS_PROVIDER:
                    if (this.loginId <= 0) {
                        this.loginId = loginId;
                        this.provider = provider;
                        this.deviceId = null;
                        return true;
                    }
                    break;
                default:
                    if (this.login3rdId <= 0) {
                        this.login3rdId = loginId;
                        this.provider3rd = provider;
                        this.deviceId = null;
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public void incrDateCount() {
        this.dateCount++;
    }

    public void incrWin() {
        this.win++;
    }

    public void incrLose() {
        this.lose++;
    }

    public void incrKill(int kill) {
        if (kill > 0)
            this.kill += kill;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
        incrDateCount();
    }


    public void logout() {
        this.logoutTime = new Date(System.currentTimeMillis());
        isOnline = false;
    }


    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public boolean isVip() {
        return vipLevel > 0;
    }

//    private void buildAssets(IQAntObject result) {
//        if (this.assetMap != null) {
//            QAntArray array = QAntArray.newInstance();
//            this.assetMap.forEach((s, integer) -> {
//                QAntObject object = QAntObject.newInstance();
//                object.putUtfString("id", s);
//                object.putInt("value", integer);
//                array.addQAntObject(object);
//            });
//            result.putQAntArray("assets", array);
//        }
//    }

    public void incTrophy(int rankPoint) {
        this.trophy += rankPoint;
        this.trophy = Math.max(0, this.trophy);
    }

    public boolean isLinkFbGG() {
        return loginId > 0 && (provider.equals("fb") || provider.equals("gg"));
    }

    public boolean isLink3rd(String key) {
        return login3rdId > 0 && provider3rd.equals(key);
    }

    public boolean isLink3rd() {
        return login3rdId > 0;
    }

    public void unlinkFbGG() {
        this.loginId = -1;
        this.provider = "";
    }

    public void setActiveHeroId(long id) {
        //TODO apply for game choose 1 active hero
    }

    public int calProduceEnergy() {
        long l = System.currentTimeMillis() - energyStartBuild;
        if (l < 0)
            return 0;
        return Math.min((int) Math.floor(l
                / 1000f / ENERGY_PRODUCE_SPEED_SEC), energyMax - energy);
    }

    public void produceEnergy() {
        int no = calProduceEnergy();
        if (no > 0) {
            long l = Math.floorMod(System.currentTimeMillis() - energyStartBuild, ENERGY_PRODUCE_SPEED_SEC);
            this.energyStartBuild = System.currentTimeMillis() - l;
            energy += no;
        }
    }

    public boolean useEnergy(int energy) {
        produceEnergy();
        if (energy > this.energy)
            return false;
        this.energy -= energy;
        return true;
    }


}
