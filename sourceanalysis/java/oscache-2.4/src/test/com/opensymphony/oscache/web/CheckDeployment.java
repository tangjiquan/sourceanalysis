/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.oscache.web;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: hani
 * Date: Jun 12, 2003
 * Time: 3:34:20 PM
 */
public class CheckDeployment {
    public static void main(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("No url specified to check");
        }

        try {
            if (!args[0].endsWith("/")) {
                args[0] = args[0] + "/";
            }

            URL url = new URL(args[0] + "oscache.txt");
            URLConnection c = url.openConnection();
            c.getInputStream();
            System.exit(0);
        } catch (java.net.MalformedURLException e) {
            System.out.println("Invalid url for oscache webapp:" + args[0]);
        } catch (ConnectException ex) {
            System.out.println("Error connecting to server at '" + args[0] + "', ensure that the webserver for the oscache example application is running");
        } catch (FileNotFoundException e) {
            System.out.println("Error connecting to webapp at '" + args[0] + "', ensure that the example-war app is deployed correctly at the specified url");
        } catch (IOException e) {
            System.out.println("Error connecting to webapp at '" + args[0] + "', ensure that the example-war app is deployed correctly at the specified url");
        }

        System.exit(1);
    }
}
