package io.appmetrica.analytics;

import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider;
import io.appmetrica.gradle.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(Parameterized.class)
public class AppOpenApiDelegationTest extends CommonTest {

    private final String deeplink;
    private final AppMetricaProxy proxy = mock(AppMetricaProxy.class);
    private final IReporter reporter = mock(IReporter.class, CALLS_REAL_METHODS);

    public AppOpenApiDelegationTest(String name, String deeplink) {
        this.deeplink = deeplink;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {"null", null},
            {"empty", ""},
            {"space", " "},
            {"ASCII whitespace", "\t\r\n"},
            {"non-breaking space U+00A0", "\u00a0"},
            {"em space U+2003", "\u2003"},
            {"zero-width space U+200B", "\u200b"},
            {"valid deeplink", "app://test"},
            {"edge spaces", " scheme://value "}
        });
    }

    @Test
    public void appMetricaForwardsOriginalInput() {
        try (MockedConstruction<AppMetricaProxy> construction = mockConstruction(AppMetricaProxy.class);
             MockedStatic<AppMetricaProxyProvider> provider = mockStatic(AppMetricaProxyProvider.class)) {
            provider.when(AppMetricaProxyProvider::getProxy).thenReturn(proxy);

            AppMetrica.reportAppOpen(deeplink);

            verify(proxy).reportAppOpen(deeplink);
            verifyNoMoreInteractions(proxy);
        }
    }

    @Test
    public void customReporterWithoutOverrideIgnoresInput() {
        assertThatCode(() -> reporter.reportAppOpen(deeplink)).doesNotThrowAnyException();
    }
}
