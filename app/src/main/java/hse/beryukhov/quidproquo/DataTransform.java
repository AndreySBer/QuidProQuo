package hse.beryukhov.quidproquo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class DataTransform {
    private static String GetTimePassed(Date date, Date now) {
        Calendar cdate = new GregorianCalendar();
        cdate.setTime(date);

        Calendar cnow = new GregorianCalendar();
        cnow.setTime(now);

        if (cdate.get(Calendar.YEAR) == cnow.get(Calendar.YEAR))//this year date
        {
            if (cdate.get(Calendar.DAY_OF_YEAR) == cnow.get(Calendar.DAY_OF_YEAR))
            //it means that the date is today
            {
                SimpleDateFormat formatToday = new SimpleDateFormat("HH:mm");
                return formatToday.format(date);
            } else {
                if (cdate.get(Calendar.DAY_OF_YEAR) == cnow.get(Calendar.DAY_OF_YEAR) - 1 &&
                        cdate.get(Calendar.YEAR) == cnow.get(Calendar.YEAR))
                //it means that the date is yesterday
                {
                    return "yesterday";
                } else
                //this year but not today and not yesterday
                {
                    SimpleDateFormat formatThisYear = new SimpleDateFormat("d MMM");
                    return formatThisYear.format(date);
                }
            }
        } else//it is too long ago
        {
            SimpleDateFormat formatNotThisYear = new SimpleDateFormat("d MMM Y");
            return formatNotThisYear.format(date);
        }
    }

    public static String GetTimePassedTillNow(Date date) {
        return GetTimePassed(date, new Date());
    }

}
