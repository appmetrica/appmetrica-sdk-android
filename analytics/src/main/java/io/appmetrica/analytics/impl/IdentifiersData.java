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
    private static final String RECEIVER = "io.appmetrica.analytics.CounterConfiguration.receiver";
    private static final String IDENTIFIERS_LIST = "io.appmetrica.analytics.CounterConfiguration.identifiersList";
    private static final String CLIDS_FOR_VERIFICATION =
        "io.appmetrica.analytics.CounterConfiguration.clidsForVerification";

    @Nullable
    private ResultReceiver mDataReceiver;
    @Nullable private List<String> mIdentifiers;
    @NonNull
    private Map<String, String> mClidsFromClientForVerification;

    public IdentifiersData(@Nullable List<String> identifiers,
                           @Nullable Map<String, String> clidsFromClientForVerification,
                           @Nullable ResultReceiver receiver) {
        mIdentifiers = identifiers;
        mDataReceiver = receiver;
        mClidsFromClientForVerification = clidsFromClientForVerification == null ? new HashMap<String, String>() :
                new HashMap<String, String>(clidsFromClientForVerification);
    }

    public boolean isStartupConsistent(@NonNull StartupState startupState) {
        return StartupRequiredUtils.containsIdentifiers(
                startupState,
                mIdentifiers,
                mClidsFromClientForVerification,
                new Function0<ClidsInfoStorage>() {
                    @Override
                    public ClidsInfoStorage invoke() {
                        return GlobalServiceLocator.getInstance().getClidsStorage();
                    }
                });
    }

    @Nullable
    public List<String> getIdentifiersList() {
        return mIdentifiers;
    }

    @NonNull
    public Map<String, String> getClidsFromClientForVerification() {
        return mClidsFromClientForVerification;
    }

    @Nullable
    public ResultReceiver getResultReceiver() {
        return mDataReceiver;
    }

    protected IdentifiersData(Parcel in) {
        Bundle bundle = in.readBundle(DataResultReceiver.class.getClassLoader());
        if (bundle != null) {
            mDataReceiver = bundle.getParcelable(RECEIVER);
            mIdentifiers = bundle.getStringArrayList(IDENTIFIERS_LIST);
            mClidsFromClientForVerification = StartupUtils.decodeClids(bundle.getString(CLIDS_FOR_VERIFICATION));
        } else {
            mClidsFromClientForVerification = new HashMap<String, String>();
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
        bundle.putParcelable(RECEIVER, mDataReceiver);
        if (mIdentifiers != null) {
            bundle.putStringArrayList(IDENTIFIERS_LIST, new ArrayList<String>(mIdentifiers));
        }
        if (mClidsFromClientForVerification != null) {
            bundle.putString(CLIDS_FOR_VERIFICATION, StartupUtils.encodeClids(mClidsFromClientForVerification));
        }
        dest.writeBundle(bundle);
    }
}
