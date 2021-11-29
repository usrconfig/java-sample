package com.seagame.ext.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LamHM
 */
public class EtokenGenerator {
    private static final int UID_LENGHT = 6;
    private static final String AB = "ABCDEFGHIGKLNMOPQRSTUVWSYZ0123456789";
    private static final SecureRandom rnd = new SecureRandom();


    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }


    public static String genEtoken() {
        return randomString(UID_LENGHT);
    }


    public static void main(String[] args) {
        List<String> codeList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String genEtoken = genEtoken();
            if (!codeList.contains(genEtoken)) {
                System.out.println(genEtoken);
                codeList.add(genEtoken);
            }
        }
    }
}
