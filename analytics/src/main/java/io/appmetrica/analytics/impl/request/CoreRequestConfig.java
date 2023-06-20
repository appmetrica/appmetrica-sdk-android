package io.appmetrica.analytics.impl.request;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.identifiers.Identifiers;
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.BaseRequestConfig;
import io.appmetrica.analytics.networktasks.internal.CommonUrlParts;

public class CoreRequestConfig extends BaseRequestConfig {

    @NonNull private String mAppDebuggable;
    @NonNull private String mAppSystem;
    @NonNull private StartupState startupState;

    @NonNull
    public String isAppDebuggable() {
        return mAppDebuggable;
    }

    void setAppDebuggable(@NonNull String appDebuggable) {
        mAppDebuggable = appDebuggable;
    }

    public String isAppSystem() {
        return mAppSystem;
    }

    void setAppSystem(@NonNull String mAppSystem) {
        this.mAppSystem = mAppSystem;
    }

    void setStartupState(@NonNull StartupState startupState) {
        this.startupState = startupState;
    }

    @Override
    public String toString() {
        return "CoreRequestConfig{" +
            "mAppDebuggable='" + mAppDebuggable + '\'' +
            ", mAppSystem='" + mAppSystem + '\'' +
            ", startupState=" + startupState +
            '}';
    }

    public static class CoreDataSource<A> extends DataSource<A> {
        @NonNull
        public final StartupState startupState;

        public CoreDataSource(@NonNull StartupState startupState, A arguments) {
            super(
                new Identifiers(
                    startupState.getUuid(),
                    startupState.getDeviceId(),
                    startupState.getDeviceIdHash()
                ), arguments
            );
            this.startupState = startupState;
        }
    }

    protected static abstract class CoreLoader<T extends CoreRequestConfig, A extends BaseRequestArguments>
            extends ComponentLoader<T, A, CoreDataSource<A>> {

        private final SafePackageManager mSafePackageManager;

        protected CoreLoader(@NonNull Context context, @NonNull String packageName) {
            this(context, packageName, new SafePackageManager());
        }

        protected CoreLoader(@NonNull Context context,
                             @NonNull String packageName,
                             @NonNull SafePackageManager safePackageManager) {
            super(context, packageName);
            mSafePackageManager = safePackageManager;
        }

        @Override
        @NonNull
        public T load(@NonNull CoreDataSource<A> dataSource) {
            T config = super.load(dataSource);

            String currentProcessPackageName = getContext().getPackageName();
            ApplicationInfo applicationInfo = mSafePackageManager.getApplicationInfo(getContext(), getPackageName(), 0);
            if (applicationInfo != null) {
                config.setAppDebuggable(getAppDebuggableState(applicationInfo));
                config.setAppSystem(getAppSystemState(applicationInfo));
            } else {
                if (TextUtils.equals(currentProcessPackageName, getPackageName())) {
                    config.setAppDebuggable(getAppDebuggableState(getContext().getApplicationInfo()));
                    config.setAppSystem(getAppSystemState(getContext().getApplicationInfo()));
                } else {
                    config.setAppDebuggable(CommonUrlParts.Values.FALSE_INTEGER);
                    config.setAppSystem(CommonUrlParts.Values.FALSE_INTEGER);
                }
            }
            config.setStartupState(dataSource.startupState);
            config.setRetryPolicyConfig(dataSource.startupState.getRetryPolicyConfig());
            return config;
        }

        @NonNull
        private String getAppDebuggableState(@NonNull ApplicationInfo info) {
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0 ?
                    CommonUrlParts.Values.TRUE_INTEGER :
                    CommonUrlParts.Values.FALSE_INTEGER;
        }

        @NonNull
        private String getAppSystemState(@NonNull ApplicationInfo info) {
            return (info.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ?
                    CommonUrlParts.Values.TRUE_INTEGER :
                    CommonUrlParts.Values.FALSE_INTEGER;
        }

    }
}
