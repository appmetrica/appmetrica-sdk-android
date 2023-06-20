package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class EmptyValueComposerTest extends CommonTest {

    private EmptyValueComposer mEmptyValueComposer = new EmptyValueComposer();

    @Test
    public void testGetValue() {
        assertThat(mEmptyValueComposer.getValue(mock(EventFromDbModel.class), mock(ReportRequestConfig.class))).isNotNull().isEmpty();
    }
}
