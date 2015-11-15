/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

/**
 *
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 * @author $Author: larst $
 * @version $Revision: 385 $
 */
public class TestFastCronParser extends TestCase {
    public TestFastCronParser(String str) {
        super(str);
    }

    /**
    * This methods returns the name of this test class to JUnit
    * <p>
    * @return The name of this class
    */
    public static Test suite() {
        return new TestSuite(TestFastCronParser.class);
    }

    /**
    * Tests to see if the cron class can calculate the previous matching
    * time correctly in various circumstances
    */
    public void testEvaluations() {
        // Minute tests
        cronCall("01/01/2003 0:00", "45 * * * *", "31/12/2002 23:45", false);
        cronCall("01/01/2003 0:00", "45-47,48,49 * * * *", "31/12/2002 23:49", false);
        cronCall("01/01/2003 0:00", "2/5 * * * *", "31/12/2002 23:57", false);

        // Hour tests
        cronCall("20/12/2003 10:00", "* 3/4 * * *", "20/12/2003 07:59", false);
        cronCall("20/12/2003 0:00", "* 3 * * *", "19/12/2003 03:59", false);

        // Day of month tests
        cronCall("07/01/2003 0:00", "30 * 1 * *", "01/01/2003 23:30", false);
        cronCall("01/01/2003 0:00", "10 * 22 * *", "22/12/2002 23:10", false);
        cronCall("01/01/2003 0:00", "30 23 19 * *", "19/12/2002 23:30", false);
        cronCall("01/01/2003 0:00", "30 23 21 * *", "21/12/2002 23:30", false);
        cronCall("01/01/2003 0:01", "* * 21 * *", "21/12/2002 23:59", false);
        cronCall("10/07/2003 0:00", "* * 30,31 * *", "30/06/2003 23:59", false);

        // Test month rollovers for months with 28,29,30 and 31 days
        cronCall("01/03/2002 0:11", "* * * 2 *", "28/02/2002 23:59", false);
        cronCall("01/03/2004 0:44", "* * * 2 *", "29/02/2004 23:59", false);
        cronCall("01/04/2002 0:00", "* * * 3 *", "31/03/2002 23:59", false);
        cronCall("01/05/2002 0:00", "* * * 4 *", "30/04/2002 23:59", false);

        // Other month tests (including year rollover)
        cronCall("01/01/2003 5:00", "10 * * 6 *", "30/06/2002 23:10", false);
        cronCall("01/01/2003 5:00", "10 * * February,April-Jun *", "30/06/2002 23:10", false);
        cronCall("01/01/2003 0:00", "0 12 1 6 *", "01/06/2002 12:00", false);
        cronCall("11/09/1988 14:23", "* 12 1 6 *", "01/06/1988 12:59", false);
        cronCall("11/03/1988 14:23", "* 12 1 6 *", "01/06/1987 12:59", false);
        cronCall("11/03/1988 14:23", "* 2,4-8,15 * 6 *", "30/06/1987 15:59", false);
        cronCall("11/03/1988 14:23", "20 * * january,FeB,Mar,april,May,JuNE,July,Augu,SEPT-October,Nov,DECEM *", "11/03/1988 14:20", false);

        // Day of week tests
        cronCall("26/06/2003 10:00", "30 6 * * 0", "22/06/2003 06:30", false);
        cronCall("26/06/2003 10:00", "30 6 * * sunday", "22/06/2003 06:30", false);
        cronCall("26/06/2003 10:00", "30 6 * * SUNDAY", "22/06/2003 06:30", false);
        cronCall("23/06/2003 0:00", "1 12 * * 2", "17/06/2003 12:01", false);
        cronCall("23/06/2003 0:00", "* * * * 3,0,4", "22/06/2003 23:59", false);
        cronCall("23/06/2003 0:00", "* * * * 5", "20/06/2003 23:59", false);
        cronCall("02/06/2003 18:30", "0 12 * * 2", "27/05/2003 12:00", false);
        cronCall("02/06/2003 18:30", "0 12 * * Tue,Thurs-Sat,2", "31/05/2003 12:00", false);
        cronCall("02/06/2003 18:30", "0 12 * * Mon-tuesday,wed,THURS-FRiday,Sat-SUNDAY", "02/06/2003 12:00", false);

        // Leap year tests
        cronCall("01/03/2003 12:00", "1 12 * * *", "28/02/2003 12:01", false); // non-leap year
        cronCall("01/03/2004 12:00", "1 12 * * *", "29/02/2004 12:01", false); // leap year
        cronCall("01/03/2003 12:00", "1 23 * * 0", "23/02/2003 23:01", false); // non-leap year
        cronCall("01/03/2004 12:00", "1 23 * * 0", "29/02/2004 23:01", false); // leap year
        cronCall("01/03/2003 12:00", "* * 29 2 *", "29/02/2000 23:59", false); // Find the previous leap-day
        cronCall("01/02/2003 12:00", "* * 29 2 *", "29/02/2000 23:59", false); // Find the previous leap-day
        cronCall("01/02/2004 12:00", "* * 29 2 *", "29/02/2000 23:59", false); // Find the previous leap-day

        // Interval and range tests
        cronCall("20/12/2003 10:00", "* */4 * * *", "20/12/2003 08:59", false);
        cronCall("20/12/2003 10:00", "* 3/2 * * *", "20/12/2003 09:59", false);
        cronCall("20/12/2003 10:00", "1-30/5 10-20/3 * jan-aug/2 *", "31/07/2003 19:26", false);
        cronCall("20/12/2003 10:00", "20-25,27-30/2 10/8 * * *", "19/12/2003 18:29", false);
    }

    /**
    * Tests a range of invalid cron expressions
    */
    public void testInvalidExpressionParsing() {
        FastCronParser parser = new FastCronParser();

        try {
            parser.setCronExpression(null);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ParseException e) {
            fail("Expected an IllegalArgumentException but received a ParseException instead");
        }

        /**
        * Not enough tokens
        */
        cronCall("01/01/2003 0:00", "", "", true);
        cronCall("01/01/2003 0:00", "8 * 8/1 *", "", true);

        /**
        * Invalid syntax
        */
        cronCall("01/01/2003 0:00", "* invalid * * *", "", true);
        cronCall("01/01/2003 0:00", "* -1 * * *", "", true);
        cronCall("01/01/2003 0:00", "* * 20 * 0", "", true);
        cronCall("01/01/2003 0:00", "* * 5-6-7 * *", "", true);
        cronCall("01/01/2003 0:00", "* * 5/6-7 * *", "", true);
        cronCall("01/01/2003 0:00", "* * 5-* * *", "", true);
        cronCall("01/01/2003 0:00", "* * 5-6* * *", "", true);
        cronCall("01/01/2003 0:00", "* * * * Mo", "", true);
        cronCall("01/01/2003 0:00", "* * * jxxx *", "", true);
        cronCall("01/01/2003 0:00", "* * * juxx *", "", true);
        cronCall("01/01/2003 0:00", "* * * fbr *", "", true);
        cronCall("01/01/2003 0:00", "* * * mch *", "", true);
        cronCall("01/01/2003 0:00", "* * * mAh *", "", true);
        cronCall("01/01/2003 0:00", "* * * arl *", "", true);
        cronCall("01/01/2003 0:00", "* * * Spteber *", "", true);
        cronCall("01/01/2003 0:00", "* * * otber *", "", true);
        cronCall("01/01/2003 0:00", "* * * nvemtber *", "", true);
        cronCall("01/01/2003 0:00", "* * * Dcmber *", "", true);
        cronCall("01/01/2003 0:00", "* * * * mnday", "", true);
        cronCall("01/01/2003 0:00", "* * * * tsdeday", "", true);
        cronCall("01/01/2003 0:00", "* * * * wdnesday", "", true);
        cronCall("01/01/2003 0:00", "* * * * frday", "", true);
        cronCall("01/01/2003 0:00", "* * * * sdhdatr", "", true);

        /**
        * Values out of range
        */
        cronCall("01/01/2003 0:00", "* * 0 * *", "", true);
        cronCall("01/01/2003 0:00", "* 50 * * *", "", true);
        cronCall("01/01/2003 0:00", "* * * 1-20 *", "", true);
        cronCall("01/01/2003 0:00", "* * 0-20 * *", "", true);
        cronCall("01/01/2003 0:00", "* * 1-40 * *", "", true);
        cronCall("01/01/2003 0:00", "* * * 1 8", "", true);
        cronCall("01/01/2003 0:00", "* * 0/3 * *", "", true);
        cronCall("01/01/2003 0:00", "* * 30 2 *", "", true); // 30th Feb doesn't ever exist!
        cronCall("01/01/2003 0:00", "* * 31 4 *", "", true); // 31st April doesn't ever exist!
    }

    /**
    * This tests the performance of the cron parsing engine. Note that it may take
    * a couple of minutes o run - by default this test is disabled. Comment out the
    * <code>return</code> statement at the start of this method to enable the
    * benchmarking.
    */
    public void testPerformance() {
        if (true) {
            //            return; // Comment out this line to benchmark
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = null;

        try {
            date = sdf.parse("21/01/2003 16:27");
        } catch (ParseException e) {
            fail("Failed to parse date. Please check your unit test code!");
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);

        long baseTime = calendar.getTimeInMillis();

        long time = 0;

        try {
            // Give HotSpot a chance to warm up
            iterate("28 17 22 02 *", baseTime, time, 10000, true);

            // Number of iterations to test
            int count = 1000000;

            // Test the best-case scenario
            long bestCaseTime = iterate("* * * * *", baseTime, time, count, true);
            System.out.println("Best case with parsing took " + bestCaseTime + "ms for " + count + " iterations. (" + (bestCaseTime / (float) count) + "ms per call)");

            // Test a near worst-case scenario
            long worstCaseTime = iterate("0-59,0-13,2,3,4,5 17-19 22-23,22,23 2,3 *", baseTime, time, count, true);
            System.out.println("Worst case with parsing took " + worstCaseTime + "ms for " + count + " iterations. (" + (worstCaseTime / (float) count) + "ms per call)");

            // Test the best-case scenario without parsing the expression on each iteration
            bestCaseTime = iterate("* * * * *", baseTime, time, count, false);
            System.out.println("Best case without parsing took " + bestCaseTime + "ms for " + count + " iterations. (" + (bestCaseTime / (float) count) + "ms per call)");

            // Test a near worst-case scenario without parsing the expression on each iteration
            worstCaseTime = iterate("0-59,0-13,2,3,4,5 17-19 22-23,22,23 2,3 *", baseTime, time, count, false);
            System.out.println("Worst case without parsing took " + worstCaseTime + "ms for " + count + " iterations. (" + (worstCaseTime / (float) count) + "ms per call)");
        } catch (ParseException e) {
        }
    }

    /**
    * Tests that a range of valid cron expressions get parsed correctly.
    */
    public void testValidExpressionParsing() {
        FastCronParser parser;

        // Check the default constructor
        parser = new FastCronParser();
        assertNull(parser.getCronExpression());

        try {
            parser = new FastCronParser("* * * * *");
            assertEquals("* * * * *", parser.getCronExpression()); // Should be the same as what we gave it
            assertEquals("* * * * *", parser.getExpressionSummary());

            parser.setCronExpression("0  *  * *     *");
            assertEquals("0  *  * *     *", parser.getCronExpression()); // Should be the same as what we gave it
            assertEquals("0 * * * *", parser.getExpressionSummary());

            parser.setCronExpression("5 10 * * 1,4,6");
            assertEquals("5 10 * * 1,4,6", parser.getExpressionSummary());

            parser.setCronExpression("0,5-20,4-15,24-27 0 *   2-4,5,6-3 *"); // Overlapping ranges, backwards ranges
            assertEquals("0,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,24,25,26,27 0 * 2,3,4,5,6 *", parser.getExpressionSummary());
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Cron expression should have been valid: " + e);
        }
    }

    /**
    * Makes a call to the FastCronParser.
    *
    * @param dateStr   The date string to use as the base date. The format must be
    * <code>"dd/MM/yyyy HH:mm"</code>.
    * @param cronExpr  The cron expression to test.
    * @param result    The expected result. This should be a date in the same format
    * as <code>dateStr</code>.
    * @param expectException Pass in <code>true</code> if the {@link FastCronParser} is
    * expected to throw a <code>ParseException</code>.
    */
    private void cronCall(String dateStr, String cronExpr, String result, boolean expectException) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = null;

        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            fail("Failed to parse date " + dateStr + ". Please check your unit test code!");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        long baseTime = calendar.getTimeInMillis();
        FastCronParser parser = null;

        try {
            parser = new FastCronParser(cronExpr);

            if (expectException) {
                fail("Should have received a ParseException while parsing " + cronExpr);
            }

            long time = parser.getTimeBefore(baseTime);
            assertEquals(result, sdf.format(new Date(time)));
        } catch (ParseException e) {
            if (!expectException) {
                fail("Unexpected ParseException while parsing " + cronExpr + ": " + e);
            }
        }
    }

    /**
    * Used by the benchmarking
    */
    private long iterate(String cronExpr, long baseTime, long time, int count, boolean withParse) throws ParseException {
        long startTime = System.currentTimeMillis();

        if (withParse) {
            FastCronParser parser = new FastCronParser();

            for (int i = 0; i < count; i++) {
                parser.setCronExpression(cronExpr);
                time = parser.getTimeBefore(baseTime);
            }
        } else {
            FastCronParser parser = new FastCronParser(cronExpr);

            for (int i = 0; i < count; i++) {
                time = parser.getTimeBefore(baseTime);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        duration += (time - time); // Use the time variable to prevent it getting optimized away
        return duration;
    }
}
