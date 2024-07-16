package ac.unindra.roemah_duren_spring.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {

    public static String strDateFromLocalDateTime(String localDateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime transDate = LocalDateTime.parse(localDateTimeString, dateTimeFormatter);

        DateTimeFormatter indonesianFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
        return indonesianFormatter.format(transDate);
    }

    public static String strDateFromLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter indonesianFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy ", new Locale("id", "ID"));
        return indonesianFormatter.format(localDateTime);
    }

    public static String strDateTimeFromLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter indonesianFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss", new Locale("id", "ID"));
        return indonesianFormatter.format(localDateTime);
    }

    public static String strDayDateFromLocalDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter indonesianFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", new Locale("id", "ID"));
        return indonesianFormatter.format(localDateTime);
    }

    public static String strDateTimeFromString(String localDateTimeString) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString, dateTimeFormatter);
        return strDateTimeFromLocalDateTime(localDateTime);
    }

}
