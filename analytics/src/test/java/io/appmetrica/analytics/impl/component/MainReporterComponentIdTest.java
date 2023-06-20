package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class MainReporterComponentIdTest extends CommonTest {

    @Test
    public void toStringMatchExpectedValue() {
        String packageName = "test.package.name";
        assertThat(new MainReporterComponentId(packageName, null).toString())
                .isEqualTo(packageName);
    }
}
