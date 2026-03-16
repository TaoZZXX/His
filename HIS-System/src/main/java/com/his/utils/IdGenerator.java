package com.his.utils;

import java.util.Random;

public class IdGenerator {
    private static final Random RANDOM = new Random();
    private static final int RANDOM_LEN = 5;

    public static Long generateNumericId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = RANDOM.nextInt((int) Math.pow(10, RANDOM_LEN));
        String randomStr = String.format("%0" + RANDOM_LEN + "d", randomNum);
        return Long.parseLong(timestamp + "");
    }
}
