package io.appmetrica.analytics.logger.internal;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YLoggerDefaultTests {

    @Test
    public void debugFlagValue() {
        assertThat(YLogger.DEBUG).isEqualTo(true);
    }

    @Test
    public void impl() {
        assertThat(YLogger.getImpl()).isNotNull();
    }
}
