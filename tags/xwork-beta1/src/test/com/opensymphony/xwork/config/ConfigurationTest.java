/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */

/**
 * Copyright (c) 2002 Notiva. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Notiva.
 * ("Confidential Information").  You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with Notiva.
 *
 * NOTIVA MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. NOTIVA SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.opensymphony.xwork.config;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.ActionProxyFactory;
import com.opensymphony.xwork.MockInterceptor;
import com.opensymphony.xwork.SimpleAction;
import com.opensymphony.xwork.config.providers.MockConfigurationProvider;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;

import junit.framework.TestCase;

import java.util.HashMap;


/**
 * ConfigurationTest
 *
 * Created : Jan 27, 2003 1:30:08 AM
 *
 * @author Jason Carreira
 */
public class ConfigurationTest extends TestCase {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void testAbstract() {
        try {
            ActionProxyFactory.getFactory().createActionProxy("/abstract", "test", null);
            fail();
        } catch (Exception e) {
            // this is what we expected
        }

        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("/nonAbstract", "test", null);
            assertTrue(proxy.getActionName().equals("test"));
            assertTrue(proxy.getConfig().getClazz().equals(SimpleAction.class));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testDefaultNamespace() {
        HashMap params = new HashMap();
        params.put("blah", "this is blah");

        HashMap extraContext = new HashMap();
        extraContext.put(ActionContext.PARAMETERS, params);

        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("/does/not/exist", "Foo", extraContext);
            proxy.execute();
            assertEquals("this is blah", proxy.getValueStack().findValue("blah"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testFileIncludeLoader() {
        RuntimeConfiguration configuration = ConfigurationManager.getConfiguration().getRuntimeConfiguration();

        // check entityTest package
        assertNotNull(configuration.getActionConfig("includeTest", "includeTest"));

        // check inheritance from Default
        assertNotNull(configuration.getActionConfig("includeTest", "Foo"));
    }

    public void testGlobalResults() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", "Foo", null);
            assertNotNull(proxy.getConfig().getResults().get("login"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInterceptorParamInehritanceOverride() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("/foo/bar", "TestInterceptorParamInehritanceOverride", null);
            assertEquals(1, proxy.getConfig().getInterceptors().size());

            MockInterceptor testInterceptor = (MockInterceptor) proxy.getConfig().getInterceptors().get(0);
            assertEquals("foo123", testInterceptor.getExpectedFoo());
            proxy.execute();
            assertTrue(testInterceptor.isExecuted());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInterceptorParamInheritance() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("/foo/bar", "TestInterceptorParamInheritance", null);
            assertEquals(1, proxy.getConfig().getInterceptors().size());

            MockInterceptor testInterceptor = (MockInterceptor) proxy.getConfig().getInterceptors().get(0);
            assertEquals("expectedFoo", testInterceptor.getExpectedFoo());
            proxy.execute();
            assertTrue(testInterceptor.isExecuted());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInterceptorParamOverride() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", "TestInterceptorParamOverride", null);
            assertEquals(1, proxy.getConfig().getInterceptors().size());

            MockInterceptor testInterceptor = (MockInterceptor) proxy.getConfig().getInterceptors().get(0);
            assertEquals("foo123", testInterceptor.getExpectedFoo());
            proxy.execute();
            assertTrue(testInterceptor.isExecuted());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testInterceptorParams() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", "TestInterceptorParam", null);
            assertEquals(1, proxy.getConfig().getInterceptors().size());

            MockInterceptor testInterceptor = (MockInterceptor) proxy.getConfig().getInterceptors().get(0);
            assertEquals("expectedFoo", testInterceptor.getExpectedFoo());
            proxy.execute();
            assertTrue(testInterceptor.isExecuted());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testMultipleConfigProviders() {
        ConfigurationManager.addConfigurationProvider(new MockConfigurationProvider());

        try {
            ConfigurationManager.getConfiguration().reload();
        } catch (ConfigurationException e) {
            e.printStackTrace();
            fail();
        }

        RuntimeConfiguration configuration = ConfigurationManager.getConfiguration().getRuntimeConfiguration();

        // check that it has configuration from xml
        assertNotNull(configuration.getActionConfig("/foo/bar", "Bar"));

        // check that it has configuration from MockConfigurationProvider
        assertNotNull(configuration.getActionConfig("", MockConfigurationProvider.FOO_ACTION_NAME));
    }

    public void testMultipleInheritance() {
        try {
            ActionProxy proxy;
            proxy = ActionProxyFactory.getFactory().createActionProxy("multipleInheritance", "test", null);
            assertNotNull(proxy);
            proxy = ActionProxyFactory.getFactory().createActionProxy("multipleInheritance", "Foo", null);
            assertNotNull(proxy);
            proxy = ActionProxyFactory.getFactory().createActionProxy("multipleInheritance", "testMultipleInheritance", null);
            assertNotNull(proxy);
            assertEquals(5, proxy.getConfig().getInterceptors().size());
            assertEquals(2, proxy.getConfig().getResults().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testPackageExtension() {
        try {
            ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("/foo/bar", "Bar", null);
            assertEquals(5, proxy.getConfig().getInterceptors().size());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        // ensure we're using the default configuration, not simple config
        XmlConfigurationProvider c = new XmlConfigurationProvider();
        ConfigurationManager.addConfigurationProvider(c);
        ConfigurationManager.getConfiguration().reload();
    }
}