/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
/*
 * Created on Nov 11, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.opensymphony.xwork.config;

import com.opensymphony.xwork.ActionInvocation;


/**
 * Resolves references declared in the action configuration from an external source
 */
public interface ExternalReferenceResolver {
    //~ Methods ////////////////////////////////////////////////////////////////

    public void resolveReferences(ActionInvocation invocation) throws ReferenceResolverException;
}
