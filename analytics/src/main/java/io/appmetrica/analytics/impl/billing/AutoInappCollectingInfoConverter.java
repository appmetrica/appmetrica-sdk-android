package io.appmetrica.analytics.impl.billing;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.AutoInappCollectingInfoProto;
import java.util.ArrayList;
import java.util.List;

public class AutoInappCollectingInfoConverter implements
        ProtobufConverter<AutoInappCollectingInfo, AutoInappCollectingInfoProto.AutoInappCollectingInfo> {

    @NonNull
    private final BillingInfoConverter billingInfoConverter;

    public AutoInappCollectingInfoConverter() {
        this(new BillingInfoConverter());
    }

    @VisibleForTesting
    AutoInappCollectingInfoConverter(@NonNull final BillingInfoConverter billingInfoConverter) {
        this.billingInfoConverter = billingInfoConverter;
    }

    @NonNull
    @Override
    public AutoInappCollectingInfoProto.AutoInappCollectingInfo fromModel(@NonNull AutoInappCollectingInfo value) {
        final AutoInappCollectingInfoProto.AutoInappCollectingInfo nano =
                new AutoInappCollectingInfoProto.AutoInappCollectingInfo();
        nano.entries = new AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo[value.billingInfos.size()];
        int index = 0;
        for (final BillingInfo billingInfo: value.billingInfos) {
            nano.entries[index] = billingInfoConverter.fromModel(billingInfo);
            index++;
        }
        nano.firstInappCheckOccurred = value.firstInappCheckOccurred;
        return nano;
    }

    @NonNull
    @Override
    public AutoInappCollectingInfo toModel(@NonNull AutoInappCollectingInfoProto.AutoInappCollectingInfo nano) {
        final List<BillingInfo> result = new ArrayList<BillingInfo>(nano.entries.length);
        for (AutoInappCollectingInfoProto.AutoInappCollectingInfo.BillingInfo entry: nano.entries) {
            result.add(billingInfoConverter.toModel(entry));
        }
        return new AutoInappCollectingInfo(result, nano.firstInappCheckOccurred);
    }
}
