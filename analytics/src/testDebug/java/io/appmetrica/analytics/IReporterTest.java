package io.appmetrica.analytics;

import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

public class IReporterTest extends CommonTest {

    @Test
    public void reportAppOpenOnCustomReporterWithoutOverrideDoesNotThrow() throws NoSuchMethodException {
        IReporter reporter = mock(IReporter.class, CALLS_REAL_METHODS);

        assertThat(IReporter.class.getMethod("reportAppOpen", String.class).isDefault()).isTrue();
        assertThatCode(() -> reporter.reportAppOpen("app://test")).doesNotThrowAnyException();
    }
}
