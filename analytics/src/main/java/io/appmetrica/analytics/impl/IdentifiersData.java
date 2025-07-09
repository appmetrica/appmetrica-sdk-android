package io.appmetrica.analytics.impl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.startup.StartupRequiredUtils;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlin.jvm.functions.Function0;

public class IdentifiersData implements Parcelable {

    public static final String BUNDLE_KEY = "io.appmetrica.analytics.impl.IdentifiersData";
    private static final String RECEIVER = "io.appmetrica.analytics.internal.CounterConfiguration.receiver";
    private static final String IDENTIFIERS_LIST =
        "io.appmetrica.analytics.internal.CounterConfiguration.identifiersList";
    private static final String CLIDS_FOR_VERIFICATION =
        "io.appmetrica.analytics.internal.CounterConfiguration.clidsForVerification";
    private static final String FORCE_REFRESH_CONFIGURATION =
        "io.appmetrica.analytics.internal.CounterConfiguration.forceRefreshConfiguration";

    @Nullable
    private ResultReceiver dataReceiver;
    @Nullable
    private List<String> identifiers;
    @NonNull
    private Map<String, String> clidsFromClientForVerification;
    private boolean forceRefreshConfiguration;

    public IdentifiersData(@Nullable List<String> identifiers,
                           @Nullable Map<String, String> clidsFromClientForVerification,
                           @Nullable ResultReceiver receiver,
                           boolean forceRefreshConfiguration) {
        this.identifiers = identifiers;
        dataReceiver = receiver;
        this.clidsFromClientForVerification = clidsFromClientForVerification == null ? new HashMap<String, String>() :
            new HashMap<String, String>(clidsFromClientForVerification);
        this.forceRefreshConfiguration = forceRefreshConfiguration;
    }

    public boolean isStartupConsistent(@NonNull StartupState startupState) {
        return StartupRequiredUtils.containsIdentifiers(
            startupState,
            identifiers,
            clidsFromClientForVerification,
            new Function0<ClidsInfoStorage>() {
                @Override
                public ClidsInfoStorage invoke() {
                    return GlobalServiceLocator.getInstance().getClidsStorage();
                }
            });
    }

    @Nullable
    public List<String> getIdentifiersList() {
        return identifiers;
    }

    @NonNull
    public Map<String, String> getClidsFromClientForVerification() {
        return clidsFromClientForVerification;
    }

    @Nullable
    public ResultReceiver getResultReceiver() {
        return dataReceiver;
    }

    public boolean isForceRefreshConfiguration() {
        return forceRefreshConfiguration;
    }

    protected IdentifiersData(Parcel in) {
        Bundle bundle = in.readBundle(DataResultReceiver.class.getClassLoader());
        if (bundle != null) {
            dataReceiver = bundle.getParcelable(RECEIVER);
            identifiers = bundle.getStringArrayList(IDENTIFIERS_LIST);
            clidsFromClientForVerification = StartupUtils.decodeClids(bundle.getString(CLIDS_FOR_VERIFICATION));
            forceRefreshConfiguration = bundle.getBoolean(FORCE_REFRESH_CONFIGURATION);
        } else {
            clidsFromClientForVerification = new HashMap<String, String>();
        }
    }

    public static final Creator<IdentifiersData> CREATOR = new Creator<IdentifiersData>() {
        @Override
        public IdentifiersData createFromParcel(Parcel in) {
            return new IdentifiersData(in);
        }

        @Override
        public IdentifiersData[] newArray(int size) {
            return new IdentifiersData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RECEIVER, dataReceiver);
        if (identifiers != null) {
            bundle.putStringArrayList(IDENTIFIERS_LIST, new ArrayList<String>(identifiers));
        }
        if (clidsFromClientForVerification != null) {
            bundle.putString(CLIDS_FOR_VERIFICATION, StartupUtils.encodeClids(clidsFromClientForVerification));
        }
        bundle.putBoolean(FORCE_REFRESH_CONFIGURATION, forceRefreshConfiguration);
        dest.writeBundle(bundle);
    }
}
