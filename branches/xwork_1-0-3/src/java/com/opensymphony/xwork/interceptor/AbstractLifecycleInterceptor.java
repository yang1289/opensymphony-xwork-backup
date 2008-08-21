/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.interceptor;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionInvocation;


/**
 * AbstractLifecycleInterceptor
 * @author Jason Carreira
 * Date: Nov 14, 2003 10:07:43 PM
 */
public abstract class AbstractLifecycleInterceptor implements Interceptor, PreResultListener {
    //~ Methods ////////////////////////////////////////////////////////////////

    /**
    * This callback method will be called after the Action execution and before the Result execution.
    * @param invocation
    * @param resultCode
    */
    public void beforeResult(ActionInvocation invocation, String resultCode) {
    }

    /**
    * Called to let an interceptor clean up any resources it has allocated.
    */
    public void destroy() {
    }

    /**
    * Called after an Interceptor is created, but before any requests are processed using the intercept() methodName. This
    * gives the Interceptor a chance to initialize any needed resources.
    */
    public void init() {
    }

    /**
    * Allows the Interceptor to do some processing on the request before and/or after the rest of the processing of the
    * request by the DefaultActionInvocation or to short-circuit the processing and just return a String return code.
    *
    * @param invocation
    * @return
    * @throws Exception
    */
    public String intercept(ActionInvocation invocation) throws Exception {
        invocation.addPreResultListener(this);

        String result = null;

        try {
            before();
            result = invocation.invoke();
            after();
        } catch (Exception e) {
            result = Action.ERROR;
            handleException(e);
        }

        return result;
    }

    /**
    * Called after the Action and Result have been executed.
    * @throws Exception
    */
    protected void after() throws Exception {
    }

    /**
    * Called before the rest of the ActionInvocation is forwarded to.
    * @throws Exception
    */
    protected void before() throws Exception {
    }

    /**
    * Called if an Exception is caught while executing the before(), the rest of the ActionInvocation, including the
    * Action and Result execution, or the after() call. The default implementation just rethrows the Exception. Subclasses
    * can choose to either throw an Exception or do some processing.
    * @param e the Exception caught
    * @throws Exception
    */
    protected void handleException(Exception e) throws Exception {
        throw e;
    }
}