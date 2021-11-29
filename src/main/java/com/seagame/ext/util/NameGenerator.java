package com.seagame.ext.util;

import com.github.javafaker.Faker;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Locale;

/**
 * @author LamHM
 */
public class NameGenerator {
    // private static final String[] locations = { "vi", "ja", "en_US", "ko",
    // "zh_CN" };
    private static final Faker[] fakers = {new Faker(new Locale("en_US")), new Faker(new Locale("ja")), new Faker(new Locale("ko"))};
    private static final String[] FIRST_NAME = {"mon", "fay", "shi", "zag", "blarg", "rash", "izen"};
    private static final String[] LAST_NAME = {"malo", "zak", "abo", "wonk"};


    public static String genName() {
        // Creates a first name with 2-3 syllables
        String firstName = "";
        int numberOfSyllablesInFirstName = RandomUtils.nextInt(2) + 2;
        for (int i = 0; i < numberOfSyllablesInFirstName; i++) {
            firstName += FIRST_NAME[RandomUtils.nextInt(FIRST_NAME.length)];
        }

        // Creates a last name with 1-2 syllables
        String lastName = "";
        int numberOfSyllablesInLastName = RandomUtils.nextInt(2) + 1;
        for (int i = 0; i < numberOfSyllablesInLastName; i++) {
            lastName += LAST_NAME[RandomUtils.nextInt(LAST_NAME.length)];
        }

        return StringUtils.capitalize(firstName) + " " + StringUtils.capitalize(lastName);
    }


    public static String fakerName() {
        return fakers[RandomUtils.nextInt(fakers.length)].name().fullName();
    }

    public static void main(String[] args) {
        String fullName = new Faker(new Locale("it")).name().fullName();
        System.out.println(fullName);
    }

}
