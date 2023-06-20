package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mockito.ArgumentMatcher;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.creation.DelegatingMethod;
import org.mockito.internal.invocation.InterceptedInvocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.mockref.MockWeakReference;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.invocation.InvocationContainer;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.internal.exceptions.Reporter.notAMockPassedToVerifyNoMoreInteractions;
import static org.mockito.internal.exceptions.Reporter.nullPassedToVerifyNoMoreInteractions;
import static org.mockito.internal.progress.ThreadSafeMockingProgress.mockingProgress;
import static org.mockito.internal.util.MockUtil.getMockHandler;

class CallProxyVerifier {

    private final static HashMap<Class<?>, Object> sCache = new HashMap<Class<?>, Object>() {
        {
            put(Context.class, RuntimeEnvironment.getApplication());
            put(String.class, "someString");
            put(Map.class, new LinkedHashMap() {
                {
                    put("a", new Object());
                }
            });
            put(Boolean.TYPE, false);
            put(Throwable.class, new Throwable());
        }
    };

    public static void verify(Object proxy, Object mock, String name, Class[] arguments) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = proxy.getClass().getMethod(name, arguments);
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Class<?> argumentClass = arguments[i];
            if (sCache.containsKey(argumentClass)) {
                args[i] = sCache.get(argumentClass);
            } else {
                args[i] = (mock(argumentClass));
            }
        }
        method.invoke(proxy, args);

        verifyInteractionsByMethodName(mock, name, arguments, args);

    }

    public static void verifyNoArgsForMock(Object proxy, Object mock, String name, Class[] argumentsProxy) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = proxy.getClass().getMethod(name, argumentsProxy);
        Object[] argsProxy = new Object[argumentsProxy.length];
        for (int i = 0; i < argumentsProxy.length; i++) {
            Class<?> argumentClass = argumentsProxy[i];
            if (sCache.containsKey(argumentClass)) {
                argsProxy[i] = sCache.get(argumentClass);
            } else {
                argsProxy[i] = (mock(argumentClass));
            }
        }
        method.invoke(proxy, argsProxy);

        verifyInteractionsByMethodName(mock, name, new Class[]{}, new Object[]{});

    }

    /*
     * Copyright (c) 2007 Mockito contributors
     * This program is made available under the terms of the MIT License.
     */
    //Based on MockitoCore.verifyNoMoreInteractions()
    private static void verifyInteractionsByMethodName(Object mock, String method, Class[] argsClasses, Object[] args) throws NoSuchMethodException {
        mockingProgress().validateState();
        try {
            if (mock == null) {
                throw nullPassedToVerifyNoMoreInteractions();
            }
            InvocationContainer invocations = getMockHandler(mock).getInvocationContainer();
            ArrayList<Object> argsList = new ArrayList<Object>(args.length);
            Collections.addAll(argsList, args);
            VerificationDataImpl data = new VerificationDataImpl((InvocationContainerImpl) invocations, new InvocationMatcher(
                    new InterceptedInvocation(new MockWeakReference<Object>(mock), new DelegatingMethod(mock.getClass().getMethod(method, argsClasses)), args, null, null, 0),
                    argsList.stream().map(new Function<Object, ArgumentMatcher>() {
                        @Override
                        public ArgumentMatcher apply(Object o) {
                            return new Equals(o);
                        }
                    }).collect(Collectors.<ArgumentMatcher>toList())
            ));
            times(1).verify(data);
        } catch (NotAMockException e) {
            throw notAMockPassedToVerifyNoMoreInteractions();
        }
    }

}
