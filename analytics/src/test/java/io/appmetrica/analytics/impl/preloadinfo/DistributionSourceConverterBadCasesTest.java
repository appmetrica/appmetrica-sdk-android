package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DistributionSourceConverterBadCasesTest extends CommonTest {

    @NonNull
    private final PreloadInfoSourceConverter preloadInfoSourceConverter = new PreloadInfoSourceConverter();

    @Test
    public void toModelBadInt() {
        assertThat(preloadInfoSourceConverter.toModel(999)).isEqualTo(DistributionSource.UNDEFINED);
    }
}
