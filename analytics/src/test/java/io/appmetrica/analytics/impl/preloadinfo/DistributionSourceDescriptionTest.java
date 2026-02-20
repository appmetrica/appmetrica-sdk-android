package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class DistributionSourceDescriptionTest extends CommonTest {

    @Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        List<Object[]> result = Arrays.asList(new Object[][]{
            {DistributionSource.UNDEFINED, "UNDEFINED"},
            {DistributionSource.APP, "APP"},
            {DistributionSource.RETAIL, "RETAIL"},
            {DistributionSource.SATELLITE, "SATELLITE"}
        });
        assert result.size() == DistributionSource.values().length;
        return result;
    }

    @NonNull
    private final DistributionSource mSource;
    @NonNull
    private final String mStringValue;

    public DistributionSourceDescriptionTest(@NonNull DistributionSource source, @NonNull String stringValue) {
        mSource = source;
        mStringValue = stringValue;
    }

    @Test
    public void fromString() {
        assertThat(mSource.getDescription()).isEqualTo(mStringValue);
    }
}
