//

//
package com.cloud.utils;

import com.cloud.utils.DateUtil.IntervalType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtilTest {

    // command line test tool
    public static void main(final String[] args) {
        final TimeZone localTimezone = Calendar.getInstance().getTimeZone();
        final TimeZone gmtTimezone = TimeZone.getTimeZone("GMT");
        final TimeZone estTimezone = TimeZone.getTimeZone("EST");

        Date time = new Date();
        System.out.println("local time :" + DateUtil.getDateDisplayString(localTimezone, time));
        System.out.println("GMT time   :" + DateUtil.getDateDisplayString(gmtTimezone, time));
        System.out.println("EST time   :" + DateUtil.getDateDisplayString(estTimezone, time));
        //Test next run time. Expects interval and schedule as arguments
        if (args.length == 2) {
            System.out.println("Next run time: " + DateUtil.getNextRunTime(IntervalType.getIntervalType(args[0]), args[1], "GMT", time).toString());
        }

        time = new Date();
        final DateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
        final String str = dfDate.format(time);
        System.out.println("Formated TZ time string : " + str);
        try {
            final Date dtParsed = DateUtil.parseTZDateString(str);
            System.out.println("Parsed TZ time string : " + dtParsed.toString());
        } catch (final ParseException e) {
            System.err.println("Parsing failed\n string : " + str + "\nexception :" + e.getLocalizedMessage());
        }
    }
}
