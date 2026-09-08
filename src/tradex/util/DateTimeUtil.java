package tradex.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DateTimeUtil {
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    public static final DateTimeFormatter TIME_ONLY_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_ONLY_FORMAT);
    }
}

