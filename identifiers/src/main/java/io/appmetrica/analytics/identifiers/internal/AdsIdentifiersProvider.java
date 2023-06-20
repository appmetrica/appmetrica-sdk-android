package io.appmetrica.analytics.identifiers.internal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.identifiers.impl.AdsIdRetriever;
import io.appmetrica.analytics.identifiers.impl.Constants;

@Keep
public class AdsIdentifiersProvider {

    private static final AdsIdRetriever retriever = new AdsIdRetriever();

    public static Bundle requestIdentifiers(@NonNull Context context, @NonNull Bundle arguments) {
        String provider = arguments.getString(Constants.PROVIDER);
        return retriever.requestId(context, provider);
    }

}
