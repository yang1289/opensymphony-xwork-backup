/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

public class ClassLoaderUtilTest extends TestCase {

    public void testGetResources() throws IOException {
        Iterator<URL> i = ClassLoaderUtil.getResources("xwork-sample.xml", ClassLoaderUtilTest.class, false);
        assertNotNull(i);
        
        assertTrue(i.hasNext());
        URL url = i.next();
        assertTrue(url.toString().endsWith("xwork-sample.xml"));
        assertTrue(!i.hasNext());
    }
    
    public void testGetResources_Multiple() throws IOException {
        Iterator<URL> i = ClassLoaderUtil.getResources("xwork-1.0.dtd", ClassLoaderUtilTest.class, false);
        assertNotNull(i);
        
        assertTrue(i.hasNext());
        URL url = i.next();
        assertTrue(url.toString().endsWith("xwork-1.0.dtd"));
        url = i.next();
        assertTrue(url.toString().endsWith("xwork-1.0.dtd"));
        assertTrue(!i.hasNext());
    }

    public void testGetResources_Aggregate() throws IOException {
        Iterator<URL> i = ClassLoaderUtil.getResources("xwork-1.0.dtd", ClassLoaderUtilTest.class, true);
        assertNotNull(i);

        assertTrue(i.hasNext());
        URL url = i.next();
        assertTrue(url.toString().endsWith("xwork-1.0.dtd"));
        url = i.next();
        assertTrue(url.toString().endsWith("xwork-1.0.dtd"));
        assertTrue(!i.hasNext());
    }

    public void testGetResources_None() throws IOException {
        Iterator<URL> i = ClassLoaderUtil.getResources("asdfasdf.html", ClassLoaderUtilTest.class, false);
        assertNotNull(i);
        
        assertTrue(!i.hasNext());
    }

    public void testGetResource() {
        URL url = ClassLoaderUtil.getResource("xwork-sample.xml", ClassLoaderUtilTest.class);
        assertNotNull(url);
        
        assertTrue(url.toString().endsWith("xwork-sample.xml"));
    }
    
    public void testGetResource_None() {
        URL url = ClassLoaderUtil.getResource("asf.xml", ClassLoaderUtilTest.class);
        assertNull(url);
    }

}