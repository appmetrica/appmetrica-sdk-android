package io.appmetrica.analytics.impl;

import org.mockito.ArgumentMatcher;

public class CounterReportMatcher implements ArgumentMatcher<CounterReport> {

    private InternalEvents mEventType;

    public static CounterReportMatcher newMatcher() {
        return new CounterReportMatcher();
    }

    private CounterReportMatcher() {
    }

    public CounterReportMatcher withType(InternalEvents type) {
        mEventType = type;
        return this;
    }

    public boolean matches(CounterReport argument) {
        return argument.getType() == mEventType.getTypeId();
    }

}
