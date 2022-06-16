package com.seagame.ext;


import com.seagame.ext.util.NetworkConstant;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Utils implements NetworkConstant {
    public static AtomicInteger atomicInteger = new AtomicInteger();

//    public static String traceSFSObjject(ISFSObject object) {
//        return object.getKeys().stream().map(s -> "/" + s + " : " + object.get(s).getObject().toString()).collect(Collectors.joining());
//    }

    public static boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static String dotToComma(String str) {
        if (str != null) {
            return String.join(",", str.split("\\."));
        }
        return str;
    }

    public static String commaToDot(String str) {
        if (str != null) {
            return String.join(".", str.split(","));
        }
        return str;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String parseWalletAddress(String deviceId) {
        String[] split = deviceId.split("#");
        if (split.length > 3) {
            return split[2];
        }
        return deviceId;
    }

    public static String getOTypeHero(String heroClass) {
        String oclass;
        switch (heroClass) {
            case "knight":
                oclass = "dark_knight";
                break;
            case "wizard":
                oclass = "dark_wizard";
                break;
            case "summoner":
                oclass = "summoner";
                break;
            case "elf":
                oclass = "fairy_elf";
                break;
            case "fighter":
                oclass = "rage_fighter";
                break;
            case "gladiator":
                oclass = "magic_gladiator";
                break;
            case "lancer":
                oclass = "grow_lancer";
                break;
            case "lord":
                oclass = "dark_lord";
                break;
            default:
                oclass = "";
                break;
        }
        return oclass;
    }

    public static String getOTypeItem(String idOrSlot) {
        String oclass;
        switch (idOrSlot) {
            case "Weapon":
                oclass = "weapon";
                break;
            case "Armor":
                oclass = "armor";
                break;
            case "Ring":
                oclass = "ring";
                break;
            case "Pendant":
                oclass = "pendant";
                break;
            case STARTER:
                oclass = "starter";
                break;
            case EGG:
                oclass = "egg";
                break;
            default:
                oclass = "";
                break;
        }
        return oclass;
    }

    public static String getOClassHero(int rarity) {
        String otype;
        switch (rarity) {
            case 1:
                otype = "b_rank";
                break;
            case 2:
                otype = "a_rank";
                break;
            case 3:
                otype = "s_rank";
                break;
            case 4:
                otype = "ss_rank";
                break;
            default:
                otype = "";
                break;
        }
        return otype;
    }

    public static String getOClassEquip(int rank) {
        String otype;
        switch (rank) {
            case 1:
                otype = "normal";
                break;
            case 2:
                otype = "exellent";
                break;
            case 3:
                otype = "ancient";
                break;
            default:
                otype = "";
                break;
        }
        return otype;
    }

    public static boolean isNotOverLap(String index) {
        return index.equals(EGG) || index.equals(STARTER);
    }
}
