package io.appmetrica.analytics.testutils;

import java.io.PrintStream;
import org.junit.rules.ExternalResource;
import org.robolectric.shadows.ShadowLog;

public class LogRule extends ExternalResource {

    private PrintStream logsStream;

    @Override
    protected void before() {
        logsStream = ShadowLog.stream;
        ShadowLog.stream = System.out;
    }

    @Override
    protected void after() {
        ShadowLog.stream = logsStream;
    }
}
