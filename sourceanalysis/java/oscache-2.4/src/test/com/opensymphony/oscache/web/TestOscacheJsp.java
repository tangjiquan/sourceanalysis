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
 * Test test the JSPs distributed with the package. It checks that the
 * cache integration is OK.
 *
 * $Id: TestOscacheJsp.java 385 2006-10-07 06:57:10Z larst $
 * @version        $Revision: 385 $
 * @author <a href="mailto:abergevin@pyxis-tech.com">Alain Bergevin</a>
 */
public final class TestOscacheJsp extends TestCase {
    // The instance of a webconversation to invoke pages
    WebConversation wc = null;
    private final String APPLICATION_SCOPE = "scope=application&";

    // Constants definition
    private final String BASE_URL_SYSTEM_PRP = "test.web.baseURL";
    private final String FIRST_PAGE = "oscacheTest.jsp?";
    private final String FORCE_CACHE_USE = "forcecacheuse=yes&";
    private final String FORCE_REFRESH = "refresh=true";
    //private final String PAGE_SCOPE = "scope=page&";
    //private final String REQUEST_SCOPE = "scope=request&";
    private final String SECOND_PAGE = "oscacheTestMultipleTagNoKey.jsp?";
    private final String SESSION_SCOPE = "scope=session&";
    private final int CACHE_TAG_EXPIRATION = 2000;
    private final int HALF_CACHE_TAG_EXPIRATION = CACHE_TAG_EXPIRATION / 2;

    /**
     * Constructor required by JUnit
     * <p>
     * @param str Test name
     */
    public TestOscacheJsp(String str) {
        super(str);
    }

    /**
     * Returns the test suite for the test class
     * <p>
     * @return   Test suite for the class
     */
    public static Test suite() {
        return new TestSuite(TestOscacheJsp.class);
    }

    /**
     * Setup method called before each testXXXX of the class
     */
    public void setUp() {
        // Create a web conversation to invoke our JSP
        if (wc == null) {
            wc = new WebConversation();
        }
    }

    /**
     * Test the cache module under load
     */
    public void testOscacheBasicForLoad() {
        String baseUrl = constructURL(FIRST_PAGE);

        // Connect to the JSP using the application scope
        String stringResponse = invokeJSP(baseUrl, CACHE_TAG_EXPIRATION);

        // Assert that a page was properly generated.
        // This does not ensure that the cache is working properly.
        // Though, it ensures that no exception or other weird problem occured
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);

        // Invoke the JSP page containing 2 cache tag
        baseUrl = constructURL(SECOND_PAGE);

        // Connect to the JSP using the application scope
        stringResponse = invokeJSP(baseUrl, CACHE_TAG_EXPIRATION);

        // Assert that a page was properly generated.
        // This does not ensure that the cache is working properly.
        // Though, it ensures that no exception or other weird problem occured
        assertTrue(stringResponse.indexOf("This is some cache content") > 0);
    }

    /**
     * Test the cache module using a JSP
     */
    public void testOscacheJsp() {
        String baseUrl = constructURL(FIRST_PAGE);

        // Connect to a session scope to allow the JSP compilation
        compileJSP(baseUrl + SESSION_SCOPE);

        // Connect to the JSP using the application scope
        String stringResponse = invokeJSP(baseUrl, HALF_CACHE_TAG_EXPIRATION);

        // Connect again, we should have the same content since it expires
        // only each 2 seconds
        assertTrue(stringResponse.equals(invokeJSP(baseUrl, HALF_CACHE_TAG_EXPIRATION)));

        // Connect again, the content should be different
        String newResponse = invokeJSP(baseUrl, CACHE_TAG_EXPIRATION + (CACHE_TAG_EXPIRATION / 4));
        assertTrue(!stringResponse.equals(newResponse));
        stringResponse = newResponse;

        // Connect again, but request the cache content so no refresh should occur
        assertTrue(stringResponse.equals(invokeJSP(baseUrl, FORCE_CACHE_USE, 0)));

        // Connect again, the content should have changed
        newResponse = invokeJSP(baseUrl, HALF_CACHE_TAG_EXPIRATION);
        assertTrue(!stringResponse.equals(newResponse));
        stringResponse = newResponse;

        // Connect for the last time, force the cache
        // refresh so the content should have changed
        assertTrue(!stringResponse.equals(invokeJSP(baseUrl, FORCE_REFRESH, 0)));

        // Invoke the JSP page containing 2 cache tag
        baseUrl = constructURL(SECOND_PAGE);
        compileJSP(baseUrl + SESSION_SCOPE);
        stringResponse = invokeJSP(baseUrl, CACHE_TAG_EXPIRATION);

        // Invoke the same page en check if it's identical
        assertTrue(stringResponse.equals(invokeJSP(baseUrl, CACHE_TAG_EXPIRATION)));
    }

    /**
     * Compile a JSP page by invoking it. We compile the page first to avoid
     * the compilation delay when testing since the time is a crucial factor
     * <p>
     * @param URL The JSP url to invoke
     */
    private void compileJSP(String URL) {
        try {
            // Invoke the JSP
            wc.getResponse(URL);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!!");
        }
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
            if (!base.endsWith("/")) {
                base = base + "/";
            }

            constructedUrl = base + Url;
        } else {
            fail("System property test.web.baseURL needs to be set to the proper server to use.");
        }

        return constructedUrl;
    }

    /**
     * Utility method to invoke a JSP page and then sleep some time before returning
     * <p>
     * @param baseUrl     The URL of the JSP to invoke
     * @param sleepTime   THe time to sleep before returning
     * @return The text value of the reponse (HTML code)
     */
    private String invokeJSP(String baseUrl, int sleepTime) {
        return invokeJSP(baseUrl, "", sleepTime);
    }

    /**
     * Utility method to invoke a JSP page and then sleep some time before returning
     * <p>
     * @param baseUrl     The URL of the JSP to invoke
     * @param URLparam    The URL parameters of the JSP to invoke
     * @param sleepTime   The time to sleep before returning
     * @return The text value of the reponse (HTML code)
     */
    private String invokeJSP(String baseUrl, String URLparam, int sleepTime) {
        try {
            // Invoke the JSP and wait the specified sleepTime
            WebResponse resp = wc.getResponse(baseUrl + APPLICATION_SCOPE + URLparam);
            Thread.sleep(sleepTime);

            return resp.getText();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception raised!!");

            return null;
        }
    }
}
