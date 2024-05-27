package io.appmetrica.analytics.gpllibrary.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.Task;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.concurrent.Executor;

public class GplLibraryWrapper implements IGplLibraryWrapper {

    private static final String TAG = "[GplLibraryWrapper]";

    static class ClientProvider {

        @NonNull
        private final Context mContext;

        ClientProvider(@NonNull Context context) {
            mContext = context;
        }

        @NonNull
        FusedLocationProviderClient createClient() throws Throwable {
            return new FusedLocationProviderClient(mContext);
        }
    }

    public enum Priority {
        PRIORITY_NO_POWER, PRIORITY_LOW_POWER, PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_HIGH_ACCURACY
    }

    public static final String FUSED_PROVIDER = "fused";

    @NonNull
    private final FusedLocationProviderClient mFusedLocationProviderClient;
    @NonNull
    private final LocationListener mLocationListener;
    @NonNull
    private final LocationCallback mLocationCallback;
    @NonNull
    private final Looper mRequestUpdatesLooper;
    @NonNull
    private final Executor mLastKnownExecutor;
    private final long mInterval;

    public GplLibraryWrapper(@NonNull Context context,
                             @NonNull final LocationListener locationListener,
                             @NonNull final Looper requestUpdatesLooper,
                             @NonNull Executor lastKnownExecutor,
                             final long interval) throws Throwable {
        this(new ClientProvider(context), locationListener, requestUpdatesLooper, lastKnownExecutor, interval);
    }

    @VisibleForTesting
    GplLibraryWrapper(@NonNull ClientProvider clientProvider,
                      @NonNull final LocationListener locationListener,
                      @NonNull final Looper requestUpdatesLooper,
                      @NonNull Executor lastKnownExecutor,
                      long interval) throws Throwable {
        mFusedLocationProviderClient = clientProvider.createClient();
        mLocationListener = locationListener;
        mRequestUpdatesLooper = requestUpdatesLooper;
        mLastKnownExecutor = lastKnownExecutor;
        mInterval = interval;
        mLocationCallback = new GplLocationCallback(mLocationListener);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void updateLastKnownLocation() throws Throwable {
        DebugLogger.INSTANCE.info(TAG, "updateLastKnownLocation");
        Task<Location> locationTask = mFusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(mLastKnownExecutor, new GplOnSuccessListener(mLocationListener));
    }

    @Override
    @SuppressLint("MissingPermission")
    public void startLocationUpdates(@NonNull Priority priority) throws Throwable {
        DebugLogger.INSTANCE.info(TAG, "startLocationUpdates");
        mFusedLocationProviderClient.requestLocationUpdates(
                LocationRequest.create()
                        .setInterval(mInterval)
                        .setPriority(getIntPriority(priority)),
                mLocationCallback,
                mRequestUpdatesLooper
        );
    }

    @Override
    public void stopLocationUpdates() throws Throwable {
        DebugLogger.INSTANCE.info(TAG, "stopLocationUpdates");
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private int getIntPriority(@NonNull Priority priority) {
        switch (priority) {
            case PRIORITY_LOW_POWER:
                return LocationRequest.PRIORITY_LOW_POWER;
            case PRIORITY_BALANCED_POWER_ACCURACY:
                return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            case PRIORITY_HIGH_ACCURACY:
                return LocationRequest.PRIORITY_HIGH_ACCURACY;
            default:
                return LocationRequest.PRIORITY_NO_POWER;
        }
    }
}
