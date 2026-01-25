package com.yxh.fangs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author:DaFu
 * @date: 2025/12/17 10:25
 */
public class SerialNumberGeneratorV2 {

    public static void main(String[] args) {
        String[] deviceIds = {"D1E", "D1E", "D1E"};
        //2024010112000099
        String[] dateTimes = {
                "20240101",
                "20240101",
                "20240101"
        };

        for (int i = 0; i < deviceIds.length; i++) {
            String deviceId = deviceIds[i];
            String dateTime = dateTimes[i];
            //dateTime 再生成8位随机数
            dateTime+=generateRandomNumber(6);

            String serial = SerialNumberGeneratorV2.generateSerial(deviceId, dateTime);
            String parsedDateTime = SerialNumberParserV2.parseDateTime(serial, deviceId);

            System.out.println("测试 " + (i+1) + ":");
            System.out.println("  设备ID: " + deviceId);
            System.out.println("  原始时间: " + dateTime);
            System.out.println("  序列号: " + serial);
            System.out.println("  解析时间: " + parsedDateTime);
            System.out.println("  结果: " + (dateTime.equals(parsedDateTime) ? "✓ 通过" : "✗ 失败"));
        }
    }

    public  static String generateRandomNumber(int i) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成序列号（保持不变）
     */
    public static String generateSerial(String deviceId, String dateTime) {
        // 步骤1: 生成val1
        String val1 = generateVal1(deviceId);

        // 步骤2: 生成val2
        String val2 = generateVal2(dateTime);

        // 步骤3: 生成最终序列号
        return generateFinalSerial(val1, val2);
    }

    /**
     * 步骤1: 将设备唯一ID使用md5生成16位小写字符串
     */
    private static String generateVal1(String deviceId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(deviceId.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 取前16位并转为小写
            return hexString.toString().substring(0, 16).toUpperCase();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 步骤2: 生成校验位和val2
     */
    private static String generateVal2(String dateTime) {
        if (dateTime == null || dateTime.length() != 14) {
            throw new IllegalArgumentException("日期时间格式必须为16位数字，例如: 20261212103029");
        }

        // 每两位一组求和
        int sum = 0;
        for (int i = 0; i < dateTime.length(); i += 2) {
            String twoDigits = dateTime.substring(i, Math.min(i + 2, dateTime.length()));
            sum += Integer.parseInt(twoDigits);
        }

        // 取后两位作为校验位
        String checkSum = String.format("%02d", sum % 100);

        // val2 = 日期字符串 + 校验位
        return dateTime + checkSum;
    }

    /**
     * 步骤3: 生成最终序列号
     */
    private static String generateFinalSerial(String val1, String val2) {
        if (val1.length() != 16 || val2.length() != 16) {
            throw new IllegalArgumentException("val1和val2长度必须为16");
        }

        StringBuilder serial = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            // 获取val1当前字符的ASCII码
            char ch1 = val1.charAt(i);

            int asciiVal = (int) ch1;

            // 获取val2当前位的数字值
            char ch2 = val2.charAt(i);
            int digitVal = Character.getNumericValue(ch2);

            // 相加并转为字符
            int sum = asciiVal + digitVal + 17;
            if (sum > 90) {
                sum += 7;
            }
            serial.append((char) (sum % 128));
            if ((i + 1) % 4 == 0 && i < 15) {
                serial.append('-');
            }
        }

        return serial.toString();
    }
}

