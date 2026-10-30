package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

public class IReporterTest extends CommonTest {

    @Test
    public void reportAppOpenIsDefaultMethod() throws NoSuchMethodException {
        assertThat(IReporter.class.getMethod("reportAppOpen", String.class).isDefault()).isTrue();
    }

    @Test
    public void customNonNullOverrideReceivesJavaNull() {
        CustomReporter reporter = mock(CustomReporter.class, CALLS_REAL_METHODS);
        reporter.reportAppOpen((String) null);
        assertThat(reporter.lastDeeplink).isNull();
    }

    abstract static class CustomReporter implements IReporter {
        String lastDeeplink = "initial";

        @Override
        public void reportAppOpen(@NonNull String deeplink) {
            lastDeeplink = deeplink;
        }
    }

}
