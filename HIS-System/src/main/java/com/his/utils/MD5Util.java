package com.his.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    public static String encrypt(String input) {
        if (input == null) {
            input = "";
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] inputBytes = input.getBytes();
            byte[] digest = md5.digest(inputBytes);

            // 将字节数组转化成字符串
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    stringBuilder.append('0');
                }
                stringBuilder.append(hex);
            }
            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密算法不存在", e);
        }
    }
}
