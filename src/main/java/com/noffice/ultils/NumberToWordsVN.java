package com.noffice.ultils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberToWordsVN {
    private static final String[] NUMBER_WORDS = {
            "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private static final String[] UNIT_WORDS = {
            "", "nghìn", "triệu", "tỷ", "nghìn tỷ", "triệu tỷ"
            // Có thể mở rộng thêm nếu cần lớn hơn nữa
    };

    public static String readNumber(Long number) {
        if (number == null) return "";
        if (number == 0) return "không";

        StringBuilder result = new StringBuilder();
        int unitIndex = 0;
        boolean hasNonZeroGroup = false;

        while (number > 0) {
            int group = (int) (number % 1000);

            if (group > 0) {
                String groupText = readThreeDigits(group, hasNonZeroGroup);
                if (!groupText.isEmpty()) {
                    if (unitIndex < UNIT_WORDS.length && !UNIT_WORDS[unitIndex].isEmpty()) {
                        groupText += " " + UNIT_WORDS[unitIndex];
                    }
                    result.insert(0, groupText + " ");
                }
                hasNonZeroGroup = true;
            } else {
                if (hasNonZeroGroup && unitIndex == 3) {
                    result.insert(0, UNIT_WORDS[unitIndex] + " ");
                }
            }

            number /= 1000;
            unitIndex++;
        }

        return result.toString().trim().replaceAll("\\s+", " ");
    }


    private static String readThreeDigits(int number, boolean hasHigherGroup) {
        int hundreds = number / 100;
        int tens = (number % 100) / 10;
        int units = number % 10;

        StringBuilder sb = new StringBuilder();

        if (hundreds > 0) {
            sb.append(NUMBER_WORDS[hundreds]).append(" trăm");
        } else if (hasHigherGroup && (tens > 0 || units > 0)) {
            sb.append("không trăm");
        }

        if (tens > 1) {
            sb.append(" ").append(NUMBER_WORDS[tens]).append(" mươi");
            if (units == 1) {
                sb.append(" mốt");
            } else if (units == 5) {
                sb.append(" lăm");
            } else if (units > 0) {
                sb.append(" ").append(NUMBER_WORDS[units]);
            }
        } else if (tens == 1) {
            sb.append(" mười");
            if (units == 5) {
                sb.append(" lăm");
            } else if (units > 0) {
                sb.append(" ").append(NUMBER_WORDS[units]);
            }
        } else if (units > 0) {
            if (hundreds > 0 || hasHigherGroup) {
                sb.append(" lẻ ");
            }
            sb.append(NUMBER_WORDS[units]);
        }

        return sb.toString().trim();
    }

    public static String convert(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) return "";

        String[] parts = numberStr.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? parts[1] : "";

        StringBuilder result = new StringBuilder();

        // Đọc phần nguyên
        long number = Long.parseLong(integerPart);
        result.append(convertNumberToWords(number));

        // Đọc phần thập phân nếu có
        if (!decimalPart.isEmpty() && !decimalPart.matches("0+")) {
            // Bỏ số 0 cuối cùng nếu nó nằm ở cuối phần thập phân
            while (decimalPart.endsWith("0")) {
                decimalPart = decimalPart.substring(0, decimalPart.length() - 1);
            }

            if (!decimalPart.isEmpty()) {
                result.append(" phẩy");
                for (char c : decimalPart.toCharArray()) {
                    int digit = Character.getNumericValue(c);
                    result.append(" ").append(NUMBER_WORDS[digit]);
                }
            }
        }

        result.append(" mét vuông");
        // Viết hoa chữ cái đầu tiên
        result.setCharAt(0, Character.toUpperCase(result.charAt(0)));

        return result.toString().replaceAll("\\s+", " ").trim();
    }

    private static String convertNumberToWords(long number) {
        if (number == 0) return "không";

        String[] unitNames = {"", "nghìn", "triệu", "tỷ"};
        StringBuilder result = new StringBuilder();

        int unitIdx = 0;
        while (number > 0) {
            int chunk = (int)(number % 1000);
            if (chunk > 0) {
                String chunkText = convertChunk(chunk);
                if (!chunkText.isEmpty()) {
                    result.insert(0, chunkText + " " + unitNames[unitIdx] + " ");
                }
            }
            number /= 1000;
            unitIdx++;
        }

        return result.toString().trim();
    }

    private static String convertChunk(int number) {
        int hundred = number / 100;
        int ten = (number % 100) / 10;
        int unit = number % 10;

        StringBuilder chunk = new StringBuilder();

        if (hundred > 0) {
            chunk.append(NUMBER_WORDS[hundred]).append(" trăm");
            if (ten == 0 && unit > 0) chunk.append(" lẻ");
        }

        if (ten > 1) {
            chunk.append(" ").append(NUMBER_WORDS[ten]).append(" mươi");
            if (unit == 1) chunk.append(" mốt");
            else if (unit == 5) chunk.append(" lăm");
            else if (unit > 0) chunk.append(" ").append(NUMBER_WORDS[unit]);
        } else if (ten == 1) {
            chunk.append(" mười");
            if (unit == 5) chunk.append(" lăm");
            else if (unit > 0) chunk.append(" ").append(NUMBER_WORDS[unit]);
        } else if (ten == 0 && unit > 0) {
            chunk.append(" ").append(NUMBER_WORDS[unit]);
        }

        return chunk.toString().trim();
    }
    
    public static String readNumberFromString(String numberStr) {
        if (numberStr == null || numberStr.isBlank()) return "";
        try {
            BigInteger number = new BigDecimal(numberStr.replace(".", "")).toBigInteger();
            return readNumberFromBigInteger(number);
        } catch (Exception e) {
            return "số không hợp lệ";
        }
    }

    public static String readNumberFromBigInteger(BigInteger number) {
        if (number == null) return "";
        if (number.equals(BigInteger.ZERO)) return "không";
        
        StringBuilder result = new StringBuilder();
        int unitIndex = 0;
        boolean hasNonZeroGroup = false;

        while (number.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divMod = number.divideAndRemainder(BigInteger.valueOf(1000));
            int group = divMod[1].intValue();
            number = divMod[0];

            if (group > 0) {
                String groupText = readThreeDigits(group, hasNonZeroGroup);
                if (!groupText.isEmpty()) {
                    if (unitIndex < UNIT_WORDS.length && !UNIT_WORDS[unitIndex].isEmpty()) {
                        groupText += " " + UNIT_WORDS[unitIndex];
                    }
                    result.insert(0, groupText + " ");
                }
                hasNonZeroGroup = true;
            } else {
                if (hasNonZeroGroup && unitIndex == 3) {
                    result.insert(0, UNIT_WORDS[unitIndex] + " ");
                }
            }

            unitIndex++;
        }

        return result.toString().trim().replaceAll("\\s+", " ");
    }

}
