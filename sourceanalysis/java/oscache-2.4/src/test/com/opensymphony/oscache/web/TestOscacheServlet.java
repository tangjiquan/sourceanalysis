/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test test the osCacheServlet distributed with the package. It checks that the
 * cache integration is OK.
 *
 * $Id: TestOscacheServlet.java 314 2005-10-16 18:30:25Z ltorunski $
 * @version        $Revision: 314 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestOscacheServlet extends TestCase {

    // The instance of a webconversation to invoke pages
    static WebConversation wc = null;
    private final String APPLICATION_SCOPE = "scope=application&";

    // Constants definition
    private final String BASE_URL_SYSTEM_PRP = "test.web.baseURL";
    private final String FORCE_CACHE_USE = "forcecacheuse=yes&";
    private final String FORCE_REFRESH = "forceRefresh=true&";
    private final String KEY = "key=ServletKeyItem&";
    private final String REFRESH_PERIOD = "refreshPeriod=";
    private final String SERVLET_URL = "/cacheServlet/?";
    private final int NO_REFRESH_WANTED = 2000;
    private final int REFRESH_WANTED = 0;

    /**
     * Constructor required by JUnit
     * <p>
     * @param str Test name
     */
    public TestOscacheServlet(String str) {
        super(str);
    }

    /**
     * Returns the test suite for the test class
     * <p>
     * @return   Test suite for the class
     */
    public static Test suite() {
        return new TestSuite(TestOscacheServlet.class);
    }

    /**
     * This method is invoked before each testXXXX methods of the
     * class. It set ups the variables required for each tests.
     */
    public void setUp() {
        // Create a web conversation on first run
        if (wc == null) {
            wc = new WebConversation();
        }
    }
    
    /**
     * Test the cache module using a servlet
     */
    public void testOscacheServlet() {
        // Make a first call just to initialize the servlet
        String newResponse = invokeServlet(NO_REFRESH_WANTED);

        // Connect to the servlet using the application scope
        String previousReponse = invokeServlet(NO_REFRESH_WANTED);

        // Call again an verify that the content hasn't changed
        newResponse = invokeServlet(NO_REFRESH_WANTED);
        assertTrue("new response " + newResponse + " should be the same to " + previousReponse, previousReponse.equals(newResponse));

        // Call again an verify that the content is updated
        newResponse = invokeServlet(REFRESH_WANTED);
        assertFalse("new response " + newResponse + " expected it to be different to last one.", previousReponse.equals(newResponse));
        previousReponse = newResponse;

        // Call short delay so content should be refresh, but it will not since
        // we ask to use the item already in cache
        newResponse = invokeServlet(REFRESH_WANTED, FORCE_CACHE_USE);
        assertTrue("new response " + newResponse + " should be the same to " + previousReponse, previousReponse.equals(newResponse));

        // Call with long delay so the item would not need refresh, but we'll ask
        // a refresh anyway
        newResponse = invokeServlet(NO_REFRESH_WANTED, FORCE_REFRESH);
        assertFalse("new response " + newResponse + " expected it to be different to last one.", previousReponse.equals(newResponse));

        // Verify that the cache key and the cache entry are present in the output and
        // that their values are correct
        assertTrue("response '" + previousReponse + "' does not contain oscache string", previousReponse.indexOf("oscache") != -1);

        assertTrue("response '" + previousReponse + "' does not contain /Test_key string", previousReponse.indexOf("/Test_key") != -1);
    }

    /**
     * Test the cache module using a servlet and basic load
     */
    public void testOscacheServletBasicForLoad() {
        // Call Servlet
        String stringResponse = invokeServlet(NO_REFRESH_WANTED);

        // Assert that a page was properly generated.
        // This does not ensure that the cache is working properly.
        // Though, it ensures that no exception or other weird problem occured
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);

        // Call again
        stringResponse = invokeServlet(REFRESH_WANTED);

        // idem comment
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);

        // Call again
        stringResponse = invokeServlet(REFRESH_WANTED, FORCE_CACHE_USE);

        // idem comment
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);

        // Call again
        stringResponse = invokeServlet(NO_REFRESH_WANTED, FORCE_REFRESH);

        // idem comment
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);
    }

    /**
     *  Reads the base url from the test.web.baseURL system property and
     *  append the given URL.
     *  <p>
     *  @param Url  Url to append to the base.
     *  @return Complete URL
     */
    private String constructURL(String Url) {
        String base = System.getProperty(BASE_URL_SYSTEM_PRP);
        String constructedUrl = null;

        if (base != null) {
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }

            constructedUrl = base + Url;
        } else {
            fail("System property test.web.baseURL needs to be set to the proper server to use.");
        }

        return constructedUrl;
    }

    /**
     * Utility method to invoke a servlet
     * <p>
     * @param refresh The time interval telling if the item needs refresh
     * @return The HTML page returned by the servlet
     */
    private String invokeServlet(int refresh) {
        // Invoke the servlet
        return invokeServlet(refresh, "");
    }

    /**
     * Utility method to invoke a servlet
     * <p>
     * @param refresh The time interval telling if the item needs refresh
     * @param URL The URL of the servlet
     * @return The HTML page returned by the servlet
     */
    private String invokeServlet(int refresh, String URL) {
        // wait 10 millis to change the time, see System.currentTimeMillis() in OscacheServlet
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignore) {
        }

        // Invoke the servlet
        try {
            String request = constructURL(SERVLET_URL) + APPLICATION_SCOPE + KEY + REFRESH_PERIOD + refresh + "&" + URL;
            WebResponse resp = wc.getResponse(request);
            return resp.getText();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised! " + ex.getMessage());
            return "";
        }
    }
}
