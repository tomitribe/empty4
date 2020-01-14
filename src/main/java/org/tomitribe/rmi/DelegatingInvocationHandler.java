/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2019
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package org.tomitribe.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DelegatingInvocationHandler implements InvocationHandler {

    private final Object delegate;

    public DelegatingInvocationHandler(Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return method.invoke(delegate, objects);
    }
}
