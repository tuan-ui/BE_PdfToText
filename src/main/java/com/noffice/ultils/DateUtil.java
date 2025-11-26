package com.noffice.ultils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    public static String formatTimeDiff(LocalDateTime createTime, LocalDateTime nowTime) {
        Duration duration = Duration.between(createTime, nowTime);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 60) {
            return "Gần đây";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return createTime.format(formatter);
        }
    }
    public static LocalDateTime parseFlexibleDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Không thể phân tích ngày: null hoặc rỗng");
        }

        try {
            ZonedDateTime zdt = ZonedDateTime.parse(input, DateTimeFormatter.RFC_1123_DATE_TIME);
            return zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                int openParenIndex = input.indexOf('(');
                String cleanInput = openParenIndex > 0
                        ? input.substring(0, openParenIndex).trim()
                        : input.trim();

                DateTimeFormatter jsFormat = DateTimeFormatter.ofPattern(
                        "EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
                ZonedDateTime zdt = ZonedDateTime.parse(cleanInput, jsFormat);
                return zdt.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            } catch (Exception ex) {  // Bắt Exception thay vì chỉ DateTimeParseException
                throw new IllegalArgumentException("Không thể phân tích ngày: " + input, ex);
            }
        }
    }

    public static String formatLocalDateToString(LocalDate date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public static String formatLocalDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static String convertStringDateFormat(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }

        // Formatter cho đầu vào và đầu ra
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(date, inputFormatter);
            return localDateTime.format(outputFormatter);
        } catch (Exception e) {
            // fallback nếu format khác
            try {
                // Nếu không parse được, thử ISO
                return OffsetDateTime.parse(date)
                        .atZoneSameInstant(ZoneId.systemDefault())
                        .format(outputFormatter);
            } catch (Exception ignored) {
                return date;
            }
        }
    }

    public static Date parseFlexibleDate2(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        dateStr = dateStr.trim();
        String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy"};

        ParseException lastException = null;
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException e) {
                lastException = e;
            }
        }

        throw new ParseException(
                "error.DateParseError",
                lastException != null ? lastException.getErrorOffset() : 0
        );
    }


}
