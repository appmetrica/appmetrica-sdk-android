package io.appmetrica.analytics.impl.selfreporting;

import androidx.annotation.NonNull;

/*
    Java 15 does not allow static final fields changing. That approach was widely used for public api testing.
    Hopefully, new mockito version could easily mock static methods.
    So I introduced a set of proxy-classes with only one purpose - allow to mock those fields.
 */
class SelfReportFacadeProvider {

    @NonNull
    public static SelfReporterWrapper getReporterWrapper() {
        return sReporterWrapper;
    }

    @NonNull
    private static final SelfReporterWrapper sReporterWrapper = new SelfReporterWrapper();

}
