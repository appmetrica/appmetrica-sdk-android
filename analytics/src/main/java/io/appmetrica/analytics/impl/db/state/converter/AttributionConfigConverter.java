package io.appmetrica.analytics.impl.db.state.converter;

import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import java.util.ArrayList;
import java.util.List;

public class AttributionConfigConverter implements ProtobufConverter<AttributionConfig,
        StartupStateProtobuf.StartupState.Attribution> {

    @NonNull
    @Override
    public StartupStateProtobuf.StartupState.Attribution fromModel(@NonNull AttributionConfig value) {
        StartupStateProtobuf.StartupState.Attribution proto =  new StartupStateProtobuf.StartupState.Attribution();
        proto.deeplinkConditions = new StartupStateProtobuf.StartupState.Attribution
                .StringPair[value.deeplinkConditions.size()];
        for (int i = 0; i < value.deeplinkConditions.size(); i++) {
            StartupStateProtobuf.StartupState.Attribution.StringPair pair =
                    new StartupStateProtobuf.StartupState.Attribution.StringPair();
            Pair<String, AttributionConfig.Filter> modelPair = value.deeplinkConditions.get(i);
            pair.key = modelPair.first;
            if (modelPair.second != null) {
                pair.filter = new StartupStateProtobuf.StartupState.Attribution.Filter();
                pair.filter = filterToProto(modelPair.second);
            }
            proto.deeplinkConditions[i] = pair;
        }
        return proto;
    }

    @NonNull
    @Override
    public AttributionConfig toModel(@NonNull StartupStateProtobuf.StartupState.Attribution nano) {
        List<Pair<String, AttributionConfig.Filter>> modelConditions =
                new ArrayList<Pair<String, AttributionConfig.Filter>>();
        for (StartupStateProtobuf.StartupState.Attribution.StringPair pair : nano.deeplinkConditions) {
            modelConditions.add(new Pair<String, AttributionConfig.Filter>(pair.key, filterToModel(pair.filter)));
        }
        return new AttributionConfig(modelConditions);
    }

    @Nullable
    private StartupStateProtobuf.StartupState.Attribution.Filter filterToProto(
            @Nullable AttributionConfig.Filter model
    ) {
        if (model == null) {
            return null;
        }
        StartupStateProtobuf.StartupState.Attribution.Filter filter =
                new StartupStateProtobuf.StartupState.Attribution.Filter();
        filter.value = model.value;
        return filter;
    }

    @Nullable
    private AttributionConfig.Filter filterToModel(
            @Nullable StartupStateProtobuf.StartupState.Attribution.Filter proto
    ) {
        if (proto == null) {
            return null;
        }
        return new AttributionConfig.Filter(proto.value);
    }
}
