/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.XWorkMessages;
import com.opensymphony.xwork.config.ConfigurationManager;
import junit.framework.TestCase;

import java.util.Locale;
import java.util.MissingResourceException;


/**
 * LocalizedTextUtilTest
 * @author Jason Carreira
 * Created Apr 20, 2003 12:07:17 AM
 */
public class LocalizedTextUtilTest extends TestCase {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void testAddDefaultResourceBundle() {
        try {
            String message = LocalizedTextUtil.findDefaultText("foo.range");
            fail("Found message when it should not be available.");
        } catch (MissingResourceException e) {
        }

        LocalizedTextUtil.addDefaultResourceBundle("com/opensymphony/xwork/SimpleAction");

        try {
            String message = LocalizedTextUtil.findDefaultText("foo.range", Locale.US);
            assertEquals("Foo Range Message", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDefaultMessage() {
        try {
            String message = LocalizedTextUtil.findDefaultText(XWorkMessages.ACTION_EXECUTION_ERROR);
            assertEquals("Error during Action invocation", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDefaultMessageOverride() {
        try {
            String message = LocalizedTextUtil.findDefaultText(XWorkMessages.ACTION_EXECUTION_ERROR);
            assertEquals("Error during Action invocation", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }

        LocalizedTextUtil.addDefaultResourceBundle("com/opensymphony/xwork/test");

        try {
            String message = LocalizedTextUtil.findDefaultText(XWorkMessages.ACTION_EXECUTION_ERROR);
            assertEquals("Testing resource bundle override", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testParameterizedDefaultMessage() {
        try {
            String message = LocalizedTextUtil.findDefaultText(XWorkMessages.MISSING_ACTION_EXCEPTION, new String[]{
                "AddUser"
            });
            assertEquals("There is no Action mapped for action name AddUser", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testParameterizedDefaultMessageWithPackage() {
        try {
            String message = LocalizedTextUtil.findDefaultText(XWorkMessages.MISSING_PACKAGE_ACTION_EXCEPTION, new String[]{
                "blah", "AddUser"
            });
            assertEquals("There is no Action mapped for namespace blah and action name AddUser", message);
        } catch (MissingResourceException e) {
            e.printStackTrace();
            fail();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        ConfigurationManager.destroyConfiguration();
        ConfigurationManager.getConfiguration().reload();
        OgnlValueStack stack = new OgnlValueStack();
        ActionContext.setContext(new ActionContext(stack.getContext()));
        ActionContext.getContext().setLocale(Locale.US);
    }
}
