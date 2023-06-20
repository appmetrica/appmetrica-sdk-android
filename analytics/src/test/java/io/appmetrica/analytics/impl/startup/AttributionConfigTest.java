package io.appmetrica.analytics.impl.startup;

import android.util.Pair;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AttributionConfigTest extends CommonTest {

    @Test
    public void constructor() throws IllegalAccessException {
        List<Pair<String, AttributionConfig.Filter>> list = mock(List.class);
        AttributionConfig config = new AttributionConfig(list);
        ObjectPropertyAssertions(config)
                .checkField("deeplinkConditions", list)
                .checkAll();
    }

    @Test
    public void filterToString() {
        AttributionConfig.Filter filter = new AttributionConfig.Filter("my value");
        assertThat(filter.toString()).isEqualTo("my value");
    }
}
