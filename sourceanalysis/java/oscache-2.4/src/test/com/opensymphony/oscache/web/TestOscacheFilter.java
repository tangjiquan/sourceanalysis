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
 * Tests the caching filter distributed with the package.
 *
 * $Id: TestOscacheFilter.java 435 2007-04-01 10:47:02Z larst $
 * @version        $Revision: 435 $
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public final class TestOscacheFilter extends TestCase {
    // The instance of a webconversation to invoke pages
    WebConversation wc = null;
    private final String BASE_PAGE = "filter/filterTest.jsp";

    // Constants definition
    private final String BASE_URL_SYSTEM_PRP = "test.web.baseURL";
    private final String PARAM_1 = "abc=123";
    private final String PARAM_2 = "xyz=321";
    private final String SESSION_ID = "jsessionid=12345678";
    // Constants definition to access OscacheServlet
    private final String SERVLET_URL = "cacheServlet/?";
    private final String FORCE_REFRESH = "forceRefresh=true&";


    /**
     * Constructor required by JUnit
     * <p>
     * @param str Test name
     */
    public TestOscacheFilter(String str) {
        super(str);
    }

    /**
     * Returns the test suite for the test class
     * <p>
     * @return   Test suite for the class
     */
    public static Test suite() {
        return new TestSuite(TestOscacheFilter.class);
    }

    /**
     * Setup method called before each testXXXX of the class
     */
    public void setUp() {
        // Create a web conversation to invoke our filter
        if (wc == null) {
            wc = new WebConversation();
        }
        compileJSP(constructURL(BASE_PAGE));
    }

    /**
     * Test the OSCache filter
     */
    public void testOscacheFilter() {
        String baseUrl = constructURL(BASE_PAGE);

        // Flush the cache to avoid getting refreshed content from previous tests
        flushCache();

        // Call the page for the second time
        String stringResponse = invokeURL(baseUrl, 200);

        // Connect again, we should have the same content
        String newResponse = invokeURL(baseUrl, 0);
        assertTrue("new response " + newResponse + " should be the same to " + stringResponse, stringResponse.equals(newResponse));

        // Try again with a session ID this time. The session ID should get filtered
        // out of the cache key so the content should be the same
        newResponse = invokeURL(baseUrl + "?" + SESSION_ID, 200);
        assertTrue("new response by a session id request " + newResponse + " should be the same to " + stringResponse, stringResponse.equals(newResponse));

        // Connect again with extra params, the content should be different
        newResponse = invokeURL(baseUrl + "?" + PARAM_1 + "&" + PARAM_2, 500);
        assertFalse("new response " + newResponse + " expected it to be different to last one.", stringResponse.equals(newResponse));

        stringResponse = newResponse;

        // Connect again with the parameters in a different order. We should still
        // get the same content.
        newResponse = invokeURL(baseUrl + "?" + PARAM_2 + "&" + PARAM_1, 0);
        assertTrue("order of parameters shouldn't change the response", stringResponse.equals(newResponse));

        // Connect again with the same parameters, but throw the session ID into
        // the mix again. The content should remain the same.
        newResponse = invokeURL(baseUrl + "?" + SESSION_ID + "&" + PARAM_1 + "&" + PARAM_2, 0);
        assertTrue("a session id shouldn't change the response either", stringResponse.equals(newResponse));
    }

    /**
     * Test the OSCache filter with fast requests
     */
    public void testOSCacheFilterFast() {
        String baseUrl = constructURL(BASE_PAGE);

        for (int i = 0; i < 10; i++) {
            // Flush the cache to avoid getting refreshed content from previous tests
            flushCache();
            // build the url
            String url = baseUrl + "?i=" + i;
            String response = invokeURL(url, 100);
            for (int j = 0; j < 5; j++) {
                String newResponse = invokeURL(url, 100);
                assertTrue("Fast: new response (i="+i+",j="+j+") " + newResponse + " should be the same to " + response, response.equals(newResponse));
            }
        }
    }

    /**
     * Test the cache module using a filter and basic load
     */
    public void testOscacheFilterBasicForLoad() {
        String baseUrl = constructURL(BASE_PAGE);

        for (int i = 0; i < 5; i++) {
            String stringResponse = invokeURL(baseUrl, 0);

            // Check we received something slightly sane
            assertTrue(stringResponse.indexOf("Current Time") > 0);
        }
    }

    /**
     * Compile a JSP page by invoking it. We compile the page first to avoid
     * the compilation delay when testing since the time is a crucial factor
     *
     * @param URL The JSP url to invoke
     */
    private void compileJSP(String URL) {
        try {
            // Invoke the URL
            wc.getResponse(URL);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!!");
        }
    }

    /** 
     * Flushes the cache to avoid recieving content from previous tests
     */
    private void flushCache() {
        String flushUrl = constructURL(SERVLET_URL + FORCE_REFRESH);
        
        String stringResponse = invokeURL(flushUrl, 0);
        
        assertTrue("Flushing the cache failed!", stringResponse.indexOf("This is some cache content") > 0);
        
        // avoid that flush time is equal to last update time of a new entry
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignore) {
        }
    }

    /**
     *  Reads the base url from the test.web.baseURL system property and
     *  append the given URL.
     *  <p>
     *  @param Url  Url to append to the base.
     *  @return Complete URL
     */
    private String constructURL(String url) {
        String base = System.getProperty(BASE_URL_SYSTEM_PRP);
        String constructedUrl = null;

        if (base != null) {
            if (!base.endsWith("/")) {
                base = base + "/";
            }

            constructedUrl = base + url;
        } else {
            fail("System property test.web.baseURL needs to be set to the proper server to use.");
        }

        return constructedUrl;
    }

    /**
     * Utility method to request a URL and then sleep some time before returning
     * <p>
     * @param url         The URL of the page to invoke
     * @param sleepTime   The time to sleep before returning
     * @return The text value of the reponse (HTML code)
     */
    private String invokeURL(String url, int sleepTime) {
        try {
            // Invoke the JSP and wait the specified sleepTime
            WebResponse resp = wc.getResponse(url);
            Thread.sleep(sleepTime);

            return resp.getText();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!!");

            return null;
        }
    }
    
}
