package com.seagame.ext.util;

import com.seagame.ext.ExtApplication;
import com.seagame.ext.exception.NameException;
import com.seagame.ext.managers.PlayerManager;
import org.apache.commons.lang3.StringUtils;

public class GameUtils {
    public static String correctPlayerName(String name) throws NameException {
        name = stringBeauty(name);
        if (name.length() < 3) {
            throw new NameException(NameException.MIN_SIZE);
        } else if (name.length() > 24) {
            throw new NameException(NameException.MAX_SIZE);
        } else if (StringUtils.containsIgnoreCase(name, "admin")) {
            throw new NameException(NameException.ALREADY);
        } else {
            PlayerManager playerManager = ExtApplication.getBean(PlayerManager.class);
            if (playerManager != null && playerManager.isExistName(name)) {
                throw new NameException(NameException.ALREADY);
            }
        }
        return name;
    }

    public static String stringBeauty(String name) {
        return name.trim().replaceAll("^ +| +$|( )+", "$1");
    }
}
