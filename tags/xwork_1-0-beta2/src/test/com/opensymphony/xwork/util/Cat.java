/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork.util;


/**
 *
 *
 * @author <a href="mailto:plightbo@cisco.com">Pat Lightbody</a>
 * @author $Author$
 * @version $Revision$
 */
public class Cat {
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final String SCIENTIFIC_NAME = "Feline";

    //~ Instance fields ////////////////////////////////////////////////////////

    Foo foo;
    String name;

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setFoo(Foo foo) {
        this.foo = foo;
    }

    public Foo getFoo() {
        return foo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}