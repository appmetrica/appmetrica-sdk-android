package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PreloadInfoSourceConverterTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
    public static Collection<Object[]> data() {
        List<Object[]> result = Arrays.asList(new Object[][]{
            {DistributionSource.UNDEFINED, PreloadInfoProto.PreloadInfoData.UNDEFINED},
            {DistributionSource.APP, PreloadInfoProto.PreloadInfoData.APP},
            {DistributionSource.RETAIL, PreloadInfoProto.PreloadInfoData.RETAIL},
            {DistributionSource.SATELLITE, PreloadInfoProto.PreloadInfoData.SATELLITE}
        });
        assert result.size() == DistributionSource.values().length;
        return result;
    }

    @NonNull
    private final DistributionSource model;
    private final int nano;
    @NonNull
    private final PreloadInfoSourceConverter preloadInfoSourceConverter;

    public PreloadInfoSourceConverterTest(@NonNull DistributionSource model, int nano) {
        this.model = model;
        this.nano = nano;
        preloadInfoSourceConverter = new PreloadInfoSourceConverter();
    }

    @Test
    public void toProto() {
        assertThat(preloadInfoSourceConverter.fromModel(model)).isEqualTo(nano);
    }

    @Test
    public void toModel() {
        assertThat(preloadInfoSourceConverter.toModel(nano)).isEqualTo(model);
    }
}
