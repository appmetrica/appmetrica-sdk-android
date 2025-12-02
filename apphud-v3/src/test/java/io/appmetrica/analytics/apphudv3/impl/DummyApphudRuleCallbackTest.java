package io.appmetrica.analytics.apphudv3.impl;

import com.apphud.sdk.ApphudPurchaseResult;
import com.apphud.sdk.internal.domain.model.Rule;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

public class DummyApphudRuleCallbackTest extends CommonTest {

    @Mock
    private Rule mockRule;

    @Mock
    private ApphudPurchaseResult mockPurchaseResult;

    private DummyApphudRuleCallback callback;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        callback = new DummyApphudRuleCallback();
    }

    @Test
    public void shouldPerformRule_returnsTrue() {
        boolean result = callback.shouldPerformRule(mockRule);
        assertThat(result).isTrue();
    }

    @Test
    public void shouldShowScreen_returnsTrue() {
        boolean result = callback.shouldShowScreen(mockRule);
        assertThat(result).isTrue();
    }

    @Test
    public void onPurchaseCompleted_doesNotThrowException() {
        // Verify that the method can be called without throwing an exception
        callback.onPurchaseCompleted(mockRule, mockPurchaseResult);
        // If we reach this point, the test passes as no exception was thrown
    }
}
