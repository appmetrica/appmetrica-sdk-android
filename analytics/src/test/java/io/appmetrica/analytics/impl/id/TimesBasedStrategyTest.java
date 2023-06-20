package io.appmetrica.analytics.impl.id;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class TimesBasedStrategyTest extends CommonTest {

    private final TimesBasedRetryStrategy strategy = new TimesBasedRetryStrategy(3, 100);

    @Test
    public void getTimeout() {
        assertThat(strategy.getTimeout()).isEqualTo(100);
    }

    @Test
    public void nextAttempt() {
        assertThat(strategy.nextAttempt()).isTrue();
        assertThat(strategy.nextAttempt()).isTrue();
        assertThat(strategy.nextAttempt()).isTrue();

        assertThat(strategy.nextAttempt()).isFalse();

        strategy.reset();
        assertThat(strategy.nextAttempt()).isTrue();
    }
}
