/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Class for a clean startup and shutdown of the ServletCacheAdministrator and its application scoped cache.
 * @author <a href="&#109;a&#105;&#108;&#116;&#111;:chris&#64;swebtec.&#99;&#111;&#109;">Chris Miller</a>
 */
public class CacheContextListener implements ServletContextListener {

    /**
     * This notification occurs when the webapp is ready to process requests.<p>
     * We use this hook to cleanly start up the {@link ServletCacheAdministrator}
     * and create the application scope cache (which will consequentially
     * initialize any listeners configured for it that implement <code>LifecycleAware</code>.)<p>
     *
     * As of Servlet 2.4, this is guaranteed to be called before any Servlet.init()
     * methods.
     */
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        ServletCacheAdministrator.getInstance(context);
    }

    /**
     * This notification occurs when the servlet context is about to be shut down.
     * We use this hook to cleanly shut down the cache.
     */
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext context = servletContextEvent.getServletContext();
        ServletCacheAdministrator.destroyInstance(context);
    }

}