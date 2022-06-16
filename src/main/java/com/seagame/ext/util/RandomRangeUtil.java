package com.seagame.ext.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LamHM
 */
public class RandomRangeUtil {

    public static String randomReward(String fullReward, int randomNo) {
        List<String> collectReward = Arrays.stream(StringUtils.split(fullReward, "#")).collect(Collectors.toList());
        Collections.shuffle(collectReward);
        return String.join("#", collectReward.subList(0, Math.min(randomNo, collectReward.size())));
    }


    public static String randomRewardV2(String fullReward, int randomNo) {
        String[] split = StringUtils.split(fullReward, "#");
        return randomInRange(split.length, randomNo).stream().map(i -> split[i]).collect(Collectors.joining("#"));
    }


    public static boolean isSuccessPerPercent(int rate, int total) {
        total = (total > 0) ? total : 100;
        return (RandomUtils.nextInt(total) + 1) <= rate;
    }


    public static Collection<Integer> randomInRange(int maxValue, int randomNo) {
        if (randomNo > maxValue)
            randomNo = maxValue;

        return new Random().ints(0, maxValue).distinct().limit(randomNo).boxed().collect(Collectors.toList());
    }

    public static Collection<Integer> nRandomInRange(int maxValue, int randomNo) {
        return new Random().ints(0, maxValue).limit(randomNo).boxed().collect(Collectors.toList());
    }


    public static int randomNPercent(List<Integer> luckyPointList, int totalRate) {
        int randomPoint = RandomUtils.nextInt(totalRate) + 1;
        int sum = 0;
        for (int i = 0; i < luckyPointList.size(); i++) {
            sum += luckyPointList.get(i);
            if (randomPoint <= sum)
                return i;
        }

        return 0;
    }

    public static boolean isSuccessPerPermille(int rate) {
        return (RandomUtils.nextInt(1000) + 1) <= rate;
    }

    public static String randomQuantity(String reward) {
        String[] split = reward.split(NetworkConstant.SEPERATE_ITEM_NO);
        String index = split[0];
        int no = 1;
        if (split.length > 2) {
            no = ranBetween(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        } else if (split.length > 1) {
            no = Integer.parseInt(split[1]);
        }
        return index + NetworkConstant.SEPERATE_ITEM_NO + no;
    }

    public static int ranBetween(int min, int max) {
        return min + RandomUtils.nextInt(max - min + 1);
    }

    public static String randomDroprate(String reward, String dropRate, int dropNumber) {
        return randomDroprate(reward, dropRate, dropNumber, 100);
    }

    public static String randomDroprate(String reward, String dropRate, int dropNumber, int total) {

        if (reward == null)
            return null;

        String[] splitReward = StringUtils.split(reward, "#");
        String[] dropRateList = StringUtils.split(dropRate, "#");
        if (dropNumber <= 0)
            dropNumber = 1;
        int maxLength = splitReward.length;
        if (maxLength < dropNumber)
            maxLength = dropNumber;
        // random reward scale 50% -> dropNumber
        if (dropRateList == null || dropRateList.length <= 0)
            return randomInRange(splitReward.length, dropNumber).stream().map(i -> splitReward[i])
                    .collect(Collectors.joining("#"));
        // random for dropRate -> dropNumber
        Set<Integer> indexes = Stream.iterate(0, i -> i + 1).limit(dropRateList.length)
                .filter(i -> isSuccessPerPercent(Integer.parseInt(dropRateList[i]), total)).collect(Collectors.toSet());
        if (indexes.size() < dropNumber) {
            Collection<Integer> moreIndexs = new Random().ints(dropNumber - indexes.size(), 0, maxLength).distinct()
                    .boxed().collect(Collectors.toList());
            indexes.addAll(moreIndexs);
        }

        return indexes.stream().map(index -> splitReward[index]).limit(dropNumber).collect(Collectors.joining("#"));

    }

    //1000/1000
    public static String randomDroprateCanEmpty(String reward, String dropRate, int dropNumber, int total) {
        if (reward == null)
            return null;
        String[] splitReward = StringUtils.split(reward, "#");
        String[] dropRateList = StringUtils.split(dropRate, "#");
        if (dropNumber <= 0)
            dropNumber = 1;
        int maxLength = splitReward.length;
        if (maxLength < dropNumber)
            maxLength = dropNumber;
        // random reward scale 50% -> dropNumber
        if (dropRateList == null || dropRateList.length <= 0)
            return randomInRange(splitReward.length, dropNumber).stream().map(i -> splitReward[i])
                    .collect(Collectors.joining("#"));
        // random for dropRate -> dropNumber
        Set<Integer> indexes = Stream.iterate(0, i -> i + 1).limit(dropRateList.length)
                .filter(i -> isSuccessPerPercent(Integer.parseInt(dropRateList[i]), total)).collect(Collectors.toSet());
//        if (indexes.size() < dropNumber) {
//            Collection<Integer> moreIndexs = new Random().ints(dropNumber - indexes.size(), 0, maxLength).distinct()
//                    .boxed().collect(Collectors.toList());
//            indexes.addAll(moreIndexs);
//        }
        return indexes.stream().map(index -> splitReward[index]).limit(dropNumber).collect(Collectors.joining("#"));

    }
}
