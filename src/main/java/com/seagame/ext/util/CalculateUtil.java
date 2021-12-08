package com.seagame.ext.util;

import com.seagame.ext.entities.hero.HeroClass;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LamHM
 */
public class CalculateUtil {
    private static final Map<String, Integer> BASE_COIN;

    static {
        BASE_COIN = new HashMap<>();
        BASE_COIN.put("S", 100);
        BASE_COIN.put("A", 80);
        BASE_COIN.put("B", 60);
        BASE_COIN.put("C", 40);
        BASE_COIN.put("D", 20);
    }


    public static int calcBattleCoins(double attackerPower, double defenderPower) {
        return (int) Math.round((Math.abs(attackerPower - defenderPower) / (attackerPower / defenderPower)) / 2000);
    }


    public static int calcBattleCoinsV2(double attackerPower, double defenderPower, String leagueRank) {
        return (int) Math.round(50 * Math.abs(defenderPower - attackerPower) / (defenderPower + attackerPower)
                + BASE_COIN.get(leagueRank));
    }


    public static int calcTrophyAttackerWin() {
        return RandomRangeUtil.ranBetween(100, 120);
    }


    public static int calcTrophyAttackerLose() {
        return RandomRangeUtil.ranBetween(50, 60);
    }

    public static int calcTrophyDefWin() {
        return RandomRangeUtil.ranBetween(110, 130);
    }


    public static int calcTrophyDefLose() {
        return RandomRangeUtil.ranBetween(40, 50);
    }


    private static float calcX(long attackerTrophy, long defenderTrophy) {
        return (float) attackerTrophy - (float) defenderTrophy;
    }




    private static int calcTrophy(long attackerTrophy, long defenderTrophy, long attackerPower, long defenderPower) {
        long l = 100 * (Math.max(defenderPower - attackerPower, 1)) / (Math.max(defenderPower + attackerPower, 1));
        int result = Math.round(-100 * calcX(attackerTrophy, defenderTrophy) / 5000
                + l + 300);

        int MinY = (int) Math.abs(Math.round(0.001 * defenderTrophy));
        int MaxY = (int) Math.abs(Math.round(0.025 * defenderTrophy));

        if (result < MinY)
            return MinY;

        if (result > MaxY)
            return MaxY;

        return result;

    }

    public static int calcPower(List<HeroClass> heroes) {
        return 50;
    }

    public static int calcSearchPower(List<HeroClass> heroes) {
        return 100;
    }

    public long betweenDates(Date firstDate, Date secondDate) throws IOException {
        return ChronoUnit.DAYS.between(firstDate.toInstant(), secondDate.toInstant());
    }


    public static void main(String[] args) {
        // System.out.println(CalculateUtil.calcBattleCoins(96840, 34916));
        // System.out.println(CalculateUtil.calcTrophyLose(1454, 44));

        // long count = Stream.iterate(0, i -> i + 1).limit(100).map(i ->
        // CalculateUtil.isSuccessPerPercent(30))
        // .filter(test -> test == true).count();
        // System.out.println(count);

        // int calcTrophyLose = calcTrophyAttackerLose(15429, 14443);
        // System.out.println(calcTrophyLose);

    }

}
