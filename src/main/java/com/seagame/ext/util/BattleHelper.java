package com.seagame.ext.util;

import com.creants.creants_2x.socket.gate.entities.IQAntArray;
import com.creants.creants_2x.socket.gate.entities.IQAntObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LamHM
 */
public class BattleHelper {

    public static Map<Long, Long> convertHpMap(IQAntArray properties) {
        Map<Long, Long> hpMap = new HashMap<>();
        if (properties != null) {
            for (int i = 0; i < properties.size(); i++) {
                IQAntObject obj = properties.getQAntObject(i);
                hpMap.put(obj.getLong("heroId"), obj.getLong("hpPercent"));
            }

        }
        return hpMap;
    }
}
