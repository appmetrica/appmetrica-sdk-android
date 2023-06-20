package io.appmetrica.analytics.coreutils.internal.logger;

import io.appmetrica.analytics.coreutils.DebugProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class YLoggerDefaultTests {

    @Test
    public void debugFlagValue() {
        assertThat(YLogger.DEBUG).isEqualTo(DebugProvider.DEBUG);
    }

    @Test
    public void impl() {
        assertThat(YLogger.getImpl()).isNotNull();
    }
}
