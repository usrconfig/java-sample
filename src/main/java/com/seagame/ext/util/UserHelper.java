package com.seagame.ext.util;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntArray;
import com.creants.creants_2x.socket.gate.entities.QAntObject;
import com.creants.creants_2x.socket.gate.wood.QAntUser;
import com.seagame.ext.ExtApplication;
import com.seagame.ext.entities.Player;
import com.seagame.ext.managers.PlayerManager;
import io.netty.util.AttributeKey;

import java.util.Collection;
import java.util.Map;

/**
 * @author LamHM
 */
public class UserHelper {
    private static final AttributeKey<Long> LOGIN_TIME = AttributeKey.valueOf("LOGIN_TIME");
    private static final AttributeKey<Long> GUILD_ID = AttributeKey.valueOf("GUILD_ID");
    // workaround do thiết kế ban đầu không trả về id của currency item cho
    // client. Nên trả về currency item như item bình thường để client tự xử lý
    private static final AttributeKey<Map<String, Long>> CURRENCY_ID_MAP = AttributeKey.valueOf("CURRENCY_MAP");


    public static Long getLoginTime(QAntUser user) {
        return user.getChannel().attr(LOGIN_TIME).get();
    }


    private static void setLoginTime(QAntUser user, long loginTime) {
        user.setLoginTime(loginTime);
        user.getChannel().attr(LOGIN_TIME).set(loginTime);
    }


    public static void login(QAntUser user, Player player) {
        UserHelper.setLoginTime(user, player.getLoginTime().getTime());
    }


    public static void setCurrencyIdMap(QAntUser user, Map<String, Long> currencyIdMap) {
        user.getChannel().attr(CURRENCY_ID_MAP).set(currencyIdMap);
    }


    public static void setGuildId(QAntUser user, Long guildId) {

        if (user != null)
            user.getChannel().attr(GUILD_ID).set(guildId);
    }


    public static Long getGuildId(QAntUser user) {
        return user.getChannel().attr(GUILD_ID).get();
    }


    public static Map<String, Long> getCurrencyIdMap(QAntUser user) {
        return user.getChannel().attr(CURRENCY_ID_MAP).get();
    }


    public static boolean isNewDate(QAntUser user) {
        return getLoginTime(user) < TimeExUtil.getNewDateMiliseconds();
    }

    public static String buildHexName() {
        return buildHexName(null, System.currentTimeMillis());
    }

    public static String buildFullHexName() {
        return buildFullHexName(null, System.currentTimeMillis());
    }

    public static String buildHexName(String prefix, long l) {
        if (prefix == null) {
            prefix = "";
        }
        return prefix + String.format("%06x", l);
    }

    public static String buildFullHexName(String prefix, long l) {
        if (prefix == null) {
            prefix = "";
        }
        return prefix + String.format("%011x", l);
    }


    private static IQAntArray buildDefaultItem(Collection<String> battleItem) {
        QAntArray qAntArray = new QAntArray();
        if (battleItem != null) {
            battleItem.forEach(s -> {
                String[] s1 = s.split("/");
                QAntObject value = new QAntObject();
                value.putUtfString("id", s1[0]);
                value.putInt("count", Integer.parseInt(s1[1]));
                qAntArray.addQAntObject(value);
            });
        }
        return qAntArray;
    }


}
