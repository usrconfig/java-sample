package com.seagame.ext.util;

import com.seagame.ext.entities.Player;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

/**
 * @author LamHM
 */
public class TimeExUtil {

    public static boolean isSameDay(Date loginTime) {
        return DateUtils.isSameDay(loginTime, new Date(System.currentTimeMillis()));
    }


    public static boolean isNewDate(Player player) {
        return player.getLoginTime().getTime() < getNewDateMiliseconds();
    }


    public static int countOffDays(Player player) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(player.getLoginTime());
        Calendar calNow = Calendar.getInstance();
        return calNow.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR);
    }


    public static long getNewDateMiliseconds() {
        return DateUtils.truncate(new Date(), Calendar.DATE).getTime();
    }




    public static void main(String[] args) throws ParseException {
        // boolean sameDay = DateUtils.isSameDay(new
        // Date(getNextValidTimeAfter("0 17 16 ? * WED")), new
        // Date(System.currentTimeMillis()));
        // System.out.println(sameDay);

        // System.out.println(DateFormatUtils.format(1547020638989L, "dd/MM/yyyy
        // HH:mm:ss"));
        // System.out.println(TimeExUtil.getNewDateMiliseconds());
        // int i = 7 * 24 * 60 * 60 * 1000;
        // int truncatedCompareTo = DateUtils.truncatedCompareTo(new
        // Date(System.currentTimeMillis() + i),
        // new Date(System.currentTimeMillis()), Calendar.DATE);
        //
        // System.out.println(truncatedCompareTo);
        //
        // System.out.println(DateFormatUtils.format(1537692095128L, "dd/MM/yyyy
        // HH:mm:ss"));

        // try {
        // getNextFireTime
        // CronExpression cron = new CronExpression("0 0 0 * * ?");
        // Date nextInvalidTimeAfter = cron.getNextInvalidTimeAfter(new Date());
        // System.out.println("*********"
        // +DateFormatUtils.format(nextInvalidTimeAfter, "dd/MM/yyyy
        // HH:mm:ss"));
        // } catch (ParseException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        // CronExpression cron = new CronExpression("0 0 0 * * ?");
        // Date nextValidTime = cron.getNextValidTimeAfter(new
        // Date(System.currentTimeMillis()));
        // System.out.println(nextValidTime);
        // long interval = nextValidTime.getTime() - System.currentTimeMillis();
        // System.out.println(interval + "/" + ((5 * 60 * 60 * 1000) + (40 * 60
        // * 1000)));

    }

}
