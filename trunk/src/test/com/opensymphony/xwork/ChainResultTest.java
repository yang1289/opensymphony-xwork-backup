/*
 * Created on 28/02/2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.opensymphony.xwork;

import junit.framework.TestCase;

import com.opensymphony.xwork.config.ConfigurationManager;


/**
 * @author CameronBraid
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChainResultTest extends TestCase {

    public void testRecursiveChain() throws Exception {

        ActionProxy proxy = ActionProxyFactory.getFactory().createActionProxy("", "InfiniteRecursionChain", null);
        try {
            proxy.execute();
            fail("did not detected repeated chain to an action");
        } catch (XworkException e) {
        }
        
    }

    protected void setUp() throws Exception {
        super.setUp();

        // ensure we're using the default configuration, not simple config
        ConfigurationManager.clearConfigurationProviders();
        ConfigurationManager.getConfiguration().reload();
    }
}
