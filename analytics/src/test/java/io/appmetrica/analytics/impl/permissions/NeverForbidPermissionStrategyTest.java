package io.appmetrica.analytics.impl.permissions;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NeverForbidPermissionStrategyTest extends CommonTest {

    @Test
    public void testAlwaysTrue() {
        assertThat(new NeverForbidPermissionStrategy().forbidUsePermission(null)).isFalse();
    }

    @Test
    public void toStringMatchExpectedValue() {
        assertThat(new NeverForbidPermissionStrategy().toString())
            .isEqualTo("AlwaysAskForPermissionStrategy{always allow permission}");
    }

}
