package com.seagame.ext.util;

import java.util.UUID;

/**
 * @author LamHM
 */
public class TransactionGenerator {

    public static String genAgentTransactionId() {
        // RFC4122
        return UUID.randomUUID().toString();
    }


    public static void main(String[] args) {
        System.out.println(TransactionGenerator.genAgentTransactionId());
    }
}
