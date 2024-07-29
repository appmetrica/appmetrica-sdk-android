package io.appmetrica.analytics.impl.crash.jvm.client;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LibraryAnrDetectorTest extends CommonTest {

    private LibraryAnrDetector mAnrDetector = new LibraryAnrDetector();

    @Test
    public void testIsAppmetricaAppmetricaAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.AppMetrica", "reportEvent", "AppMetrica.java", 12));
        assertThat(mAnrDetector.isAppmetricaAnr(stacktrace)).isTrue();
    }

    @Test
    public void testIsPushAppmetricaAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.AppMetrica", "reportEvent", "AppMetrica.java", 12));
        assertThat(mAnrDetector.isPushAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsAppmetricaNonAppmetricaAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("ru.yandex.metrica.Application", "onCreate", "Application.java", 12));
        assertThat(mAnrDetector.isAppmetricaAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsPushNonAppmetricaAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("ru.yandex.metrica.Application", "onCreate", "Application.java", 12));
        assertThat(mAnrDetector.isPushAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsAppmetricaPushAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.push.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isAppmetricaAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsPushPushAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.push.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isPushAnr(stacktrace)).isTrue();
    }

    @Test
    public void testIsAppmetricaAlmostAppmetricaAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analyticsaaa.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isAppmetricaAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsPushAlmostPushAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.pushhh.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isPushAnr(stacktrace)).isFalse();
    }

    @Test
    public void testIsAppmetricaAppmetricaInnerPackageAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.impl.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isAppmetricaAnr(stacktrace)).isTrue();
    }

    @Test
    public void testIsPushPushInnerPackageAnr() {
        List<StackTraceElement> stacktrace = Collections.singletonList(new StackTraceElement("io.appmetrica.analytics.push.impl.SomeClass", "someMethod", "SomeClass.java", 12));
        assertThat(mAnrDetector.isPushAnr(stacktrace)).isTrue();
    }
}
