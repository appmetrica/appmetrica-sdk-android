package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DistributionSourceFromStringTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{1} to {0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> result = Arrays.asList(new Object[][]{
            {DistributionSource.UNDEFINED, "UNDEFINED"},
            {DistributionSource.APP, "APP"},
            {DistributionSource.RETAIL, "RETAIL"},
            {DistributionSource.SATELLITE, "SATELLITE"},
            {DistributionSource.UNDEFINED, "bad string"},
            {DistributionSource.UNDEFINED, null}
        });
        assert result.size() == DistributionSource.values().length + 2;
        return result;
    }

    @NonNull
    private final DistributionSource mSource;
    @Nullable
    private final String mStringValue;

    public DistributionSourceFromStringTest(@NonNull DistributionSource source, @Nullable String stringValue) {
        mSource = source;
        mStringValue = stringValue;
    }

    @Test
    public void fromString() {
        assertThat(DistributionSource.fromString(mStringValue)).isEqualTo(mSource);
    }
}
