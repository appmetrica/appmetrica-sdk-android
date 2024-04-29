package io.appmetrica.analytics.impl.proxy;

/*
    Java 15 does not allow static final fields changing. That approach was widely used for public api testing.
    Hopefully, new mockito version could easily mock static methods.
    So I introduced a set of proxy-classes with only one purpose - allow to mock those fields.
 */
public class AppMetricaProxyProvider {

    private static final AppMetricaProxy sProxy = new AppMetricaProxy();

    public static AppMetricaProxy getProxy() {
        return sProxy;
    }

}
