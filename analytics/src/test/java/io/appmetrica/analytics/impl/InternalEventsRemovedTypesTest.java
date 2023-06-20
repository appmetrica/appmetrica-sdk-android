package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class InternalEventsRemovedTypesTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {2048}, // METRIKALIB-8502
            {2049}, // METRIKALIB-8502
            {2050}, // METRIKALIB-8502
            {2304}, // METRIKALIB-8488
            {4096},
            {12291}, // METRIKALIB-8381
            {12292}, // METRIKALIB-8381
            {1792}, // METRIKALIB-8472
            {5}, // METRIKALIB-8685
            {768}, // METRIKALIB-8685
            {769}, // METRIKALIB-8685
            {5888}, // METRIKALIB-8685
            {1793}, // METRIKALIB-8470
            {5893}, // METRIKALIB-8796,
            {5894}, // METRIKALIB-8796
            {5895}, // METRIKALIB-8796
        });
    }

    private final int forbiddenType;

    public InternalEventsRemovedTypesTest(final int forbiddenType) {
        this.forbiddenType = forbiddenType;
    }

    @Test
    public void noActualEventUsesForbiddenType() {
        for (InternalEvents event : InternalEvents.values()) {
            assertThat(event.getTypeId()).isNotEqualTo(forbiddenType);
        }
    }
}
