package com.seagame.ext.config.game;

import com.creants.creants_2x.core.util.AppConfig;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LamHM
 */
public class SysConfig {
    private static SysConfig instance;
    Map<String, String> attribute;

    public static SysConfig getInstance() {
        if (instance == null)
            instance = new SysConfig();
        return instance;
    }


    private SysConfig() {
        attribute = new HashMap<>();
    }


    public String getKeyCfg(String key, String defaultVal) {
        String property = AppConfig.getProps().getProperty(key, null);
        if (property == null) {
            return defaultVal;
        } else {
            if (property.startsWith("${")) {
                if (!System.getenv().containsKey(StringUtils.substringBetween(property, "${", "}")))
                    return defaultVal;
                if (!attribute.containsKey(key)) {
                    attribute.put(key, buildVal(StringUtils.substringBetween(property, "${", "}")));
                }
            } else {
                if (!attribute.containsKey(key)) {
                    attribute.put(key, property);
                }
            }
        }
        return attribute.get(key);
    }

    private String buildVal(String key) {
        return System.getenv(key);
    }

}
