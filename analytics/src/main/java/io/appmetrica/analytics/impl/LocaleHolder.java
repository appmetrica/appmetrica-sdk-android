package io.appmetrica.analytics.impl;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.system.LocaleProvider;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LocaleHolder implements LocaleProvider {

    private static volatile LocaleHolder sInstance;
    private final static Object sLock = new Object();

    public interface Listener {
        void onLocalesUpdated();
    }

    public static LocaleHolder getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new LocaleHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private List<String> locales;
    @NonNull
    private final List<Listener> listeners = new ArrayList<>();

    @VisibleForTesting
    LocaleHolder(Context context) {
        synchronized (this) {
            locales = extractLocales(context.getResources().getConfiguration());
        }
    }

    public void updateLocales(@NonNull Configuration configuration) {
        List<Listener> listeners;
        synchronized (this) {
            locales = extractLocales(configuration);
            listeners = new ArrayList<>(this.listeners);
        }
        for (Listener listener : listeners) {
            listener.onLocalesUpdated();
        }
    }

    @Override
    @NonNull
    public List<String> getLocales() {
        return locales;
    }

    public synchronized void registerLocaleUpdatedListener(@NonNull Listener listener) {
        listeners.add(listener);
    }

    private List<String> extractLocales(@NonNull Configuration configuration) {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.N)) {
            return LocalesHelperForN.getLocales(configuration);
        } else {
            return Collections.singletonList(PhoneUtils.normalizedLocale(configuration.locale));
        }
    }
}
