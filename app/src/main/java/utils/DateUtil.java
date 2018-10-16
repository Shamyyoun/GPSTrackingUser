package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shamyyoun on 2/22/2015.
 */
public class DateUtil {
    public static Date convertToDate(String strDate, String strFormat) {
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat(strFormat);
            date = format.parse(strDate);
        } catch (ParseException e) {
            date = null;
            e.printStackTrace();
        }

        return date;
    }

    public static String convertToString(Date date, String strFormat) {
        String strDate;
        try {
            SimpleDateFormat format = new SimpleDateFormat(strFormat);
            strDate = format.format(date);
        } catch (Exception e) {
            strDate = null;
            e.printStackTrace();
        }

        return strDate;
    }

    /**
     * method, used to calculate and return duration between two dates
     */
    public static long[] getDuration(Date startDate, Date endDate) {
        // set default values
        long seconds = 0;
        long minutes = 0;
        long hours = 0;
        long days = 0;

        // check current and expire
        if (endDate.getTime() > startDate.getTime()) {
            long remain = endDate.getTime() - startDate.getTime();
            seconds = remain / 1000 % 60;
            minutes = remain / (60 * 1000) % 60;
            hours = remain / (60 * 60 * 1000) % 24;
            days = remain / (24 * 60 * 60 * 1000);
        }

        return new long[]{seconds, minutes, hours, days};
    }
}
