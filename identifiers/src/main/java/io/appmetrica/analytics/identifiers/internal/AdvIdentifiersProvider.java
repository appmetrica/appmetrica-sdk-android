package io.appmetrica.analytics.identifiers.internal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.identifiers.impl.AdvIdRetriever;
import io.appmetrica.analytics.identifiers.impl.Constants;

@Keep
public class AdvIdentifiersProvider {

    private static final AdvIdRetriever retriever = new AdvIdRetriever();

    public static Bundle requestIdentifiers(@NonNull Context context, @NonNull Bundle arguments) {
        String provider = arguments.getString(Constants.PROVIDER);
        return retriever.requestId(context, provider);
    }

}
