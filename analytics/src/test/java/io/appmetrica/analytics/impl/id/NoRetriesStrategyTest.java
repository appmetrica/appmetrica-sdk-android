package io.appmetrica.analytics.impl.id;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NoRetriesStrategyTest extends CommonTest {

    private final NoRetriesStrategy noRetriesStrategy = new NoRetriesStrategy();

    @Test
    public void getTimeout() {
        assertThat(noRetriesStrategy.getTimeout()).isZero();
    }

    @Test
    public void nextAttempt() {
        assertThat(noRetriesStrategy.nextAttempt()).isTrue();
        assertThat(noRetriesStrategy.nextAttempt()).isFalse();

        noRetriesStrategy.reset();
        assertThat(noRetriesStrategy.nextAttempt()).isTrue();
    }
}
