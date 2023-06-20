package io.appmetrica.analytics.impl.preloadinfo;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.protobuf.client.PreloadInfoProto;

public class PreloadInfoSourceConverter implements Converter<DistributionSource, Integer> {

    @NonNull
    @Override
    public Integer fromModel(@NonNull DistributionSource value) {
        switch (value) {
            case APP:
                return PreloadInfoProto.PreloadInfoData.APP;
            case RETAIL:
                return PreloadInfoProto.PreloadInfoData.RETAIL;
            case SATELLITE:
                return PreloadInfoProto.PreloadInfoData.SATELLITE;
            default:
                return PreloadInfoProto.PreloadInfoData.UNDEFINED;
        }
    }

    @NonNull
    @Override
    public DistributionSource toModel(@NonNull Integer nano) {
        switch (nano) {
            case PreloadInfoProto.PreloadInfoData.APP:
                return DistributionSource.APP;
            case PreloadInfoProto.PreloadInfoData.RETAIL:
                return DistributionSource.RETAIL;
            case PreloadInfoProto.PreloadInfoData.SATELLITE:
                return DistributionSource.SATELLITE;
            default:
                return DistributionSource.UNDEFINED;
        }
    }
}
