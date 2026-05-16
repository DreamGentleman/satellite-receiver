package com.yxh.fangs.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class LicenseSerialParser {

    /**
     * 解析序列号获取日期时间
     *
     * @param serial   序列号（16位字符串）
     * @param deviceId 设备唯一ID
     * @return 日期时间字符串（16位），解析失败返回null
     */
    public static String parseDateTime(String serial, String deviceId) {
        if (serial == null || deviceId == null) {
            return null;
        }
        serial = serial.replace("-", "");
        if (serial.length() != 16) {
            return null;
        }

        try {
            // 1. 将设备唯一ID进行MD5加密，取16位字符串，记作val3
            String val3 = generateVal3(deviceId);

            // 2. 将val3的每一位转为ASCII码的数字
            int[] val3Ascii = new int[16];
            for (int i = 0; i < 16; i++) {
                val3Ascii[i] = (int) val3.charAt(i);
            }

            // 3. 将序列号的每一位也转换为ASCII码的数字
            int[] serialAscii = new int[16];
            for (int i = 0; i < 16; i++) {
                serialAscii[i] = (int) serial.charAt(i);
                if (serialAscii[i] > 90) {
                    serialAscii[i] -= 7;
                }
            }

            // 4. 将序列号转换的每一位数字减去val3转换的每一位数字，得到16位数字，记作val4
            int[] val4Digits = new int[16];
            for (int i = 0; i < 16; i++) {
                val4Digits[i] = serialAscii[i] - val3Ascii[i] - 17;

                // 处理可能出现的负数（由于ASCII码范围限制）
                while (val4Digits[i] < 0) {
                    val4Digits[i] += 128;
                }

                // 确保是单个数字（0-9）
                val4Digits[i] = val4Digits[i] % 10;
            }

            // 5. 检查val4中前16位的校验和与后两位校验位是否相等
            // 计算前16位每两位一组的和
            int sum = 0;
            StringBuilder dateTimeStr = new StringBuilder();

            // 前16位转为字符串
            for (int i = 0; i < 14; i++) {
                dateTimeStr.append(val4Digits[i]);
                if (i % 2 == 1 && i < 13) {
                    // 每两位组合成一个数字并求和
                    int twoDigit = val4Digits[i - 1] * 10 + val4Digits[i];
                    sum += twoDigit;
                }
            }

            // 处理最后两位（第13和14位）
            if (val4Digits.length >= 14) {
                int lastTwoDigit = val4Digits[12] * 10 + val4Digits[13];
                sum += lastTwoDigit;
            }

            // 取和的最后两位作为计算出的校验和
            int calculatedChecksum = sum % 100;

            // 获取val4的后两位作为校验位
            int checksumFromVal4 = 0;
            if (val4Digits.length >= 14) {
                checksumFromVal4 = val4Digits[14] * 10 + val4Digits[15];
            }

            // 6. 验证校验和
            if (calculatedChecksum == checksumFromVal4) {
                return dateTimeStr.toString();
            } else {
                System.out.println("校验失败: 计算值=" + calculatedChecksum + ", 校验位=" + checksumFromVal4);
                return null;
            }

        } catch (Exception e) {
            System.err.println("解析序列号时发生错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成val3: 将设备唯一ID进行MD5加密，取16位字符串
     */
    private static String generateVal3(String deviceId) {
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

            // 取前16位并转为小写（与生成时的val1相同）
            return hexString.toString().substring(0, 16).toUpperCase(Locale.ROOT);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 验证序列号
     */
    public static boolean validateSerial(String serial, String deviceId, String expectedDateTime) {
        String parsedDateTime = parseDateTime(serial, deviceId);
        if (parsedDateTime == null) {
            return false;
        }
        return parsedDateTime.equals(expectedDateTime);
    }

    /**
     * 解析序列号的详细信息
     */
    public static LicenseSerialParser.SerialInfo parseSerialInfo(String serial, String deviceId) {
        LicenseSerialParser.SerialInfo info = new LicenseSerialParser.SerialInfo();
        info.setSerial(serial);
        info.setDeviceId(deviceId);

        String dateTime = parseDateTime(serial, deviceId);
        if (dateTime != null) {
            info.setDateTime(dateTime);
            info.setValid(true);
            info.setParsedInfo("解析成功: 日期时间=" + dateTime);
        } else {
            info.setValid(false);
            info.setParsedInfo("解析失败: 校验未通过");
        }

        return info;
    }

    /**
     * 序列号信息类
     */
    public static class SerialInfo {
        private String serial;
        private String deviceId;
        private String dateTime;
        private boolean valid;
        private String parsedInfo;

        // Getters and Setters
        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDateTime() {
            return dateTime;
        }

        public void setDateTime(String dateTime) {
            this.dateTime = dateTime;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getParsedInfo() {
            return parsedInfo;
        }

        public void setParsedInfo(String parsedInfo) {
            this.parsedInfo = parsedInfo;
        }

        @Override
        public String toString() {
            return "SerialInfo{" +
                    "serial='" + serial + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    ", dateTime='" + dateTime + '\'' +
                    ", valid=" + valid +
                    ", parsedInfo='" + parsedInfo + '\'' +
                    '}';
        }
    }
}
