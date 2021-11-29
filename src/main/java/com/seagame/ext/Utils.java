package com.seagame.ext;


import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Utils {
    public static AtomicInteger atomicInteger = new AtomicInteger();

//    public static String traceSFSObjject(ISFSObject object) {
//        return object.getKeys().stream().map(s -> "/" + s + " : " + object.get(s).getObject().toString()).collect(Collectors.joining());
//    }

    public static boolean notEmpty(String str) {
        return str != null && !str.isEmpty();
    }
    public static String dotToComma(String str) {
        if(str!=null){
            return String.join(",", str.split("\\."));
        }
        return str;
    }
    public static String commaToDot(String str) {
        if(str!=null){
            return String.join(".", str.split(","));
        }
        return str;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
