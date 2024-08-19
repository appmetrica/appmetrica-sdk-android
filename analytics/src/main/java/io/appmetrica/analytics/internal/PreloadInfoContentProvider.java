package io.appmetrica.analytics.internal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper;
import io.appmetrica.analytics.impl.ContentProviderHelper;
import io.appmetrica.analytics.impl.SdkUtils;
import io.appmetrica.analytics.impl.preloadinfo.ContentProviderHelperFactory;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class PreloadInfoContentProvider extends ContentProvider {

    private static final String TAG = "[PreloadInfoContentProvider]";
    private static final String AUTHORITY_SUFFIX = ".appmetrica.preloadinfo.retail";
    private static final int PRELOAD_INFO_LIST = 1;
    private static final int CLIDS = 2;

    private boolean disabled = false;

    private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        DebugLogger.INSTANCE.info(TAG, "onCreate");
        final Context context = getApplicationContext();
        final String authorityPrefix;
        if (context != null) {
            authorityPrefix = context.getPackageName();
        } else {
            authorityPrefix = "";
            DebugLogger.INSTANCE.info(TAG, "Could not form authority: context is null");
        }
        String authority = authorityPrefix + AUTHORITY_SUFFIX;
        DebugLogger.INSTANCE.info(TAG, "authority: " + authority);
        uriMatcher.addURI(authority, "preloadinfo", PRELOAD_INFO_LIST);
        uriMatcher.addURI(authority, "clids", CLIDS);
        ContentProviderFirstLaunchHelper.onCreate(this);
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        synchronized (this) {
            if (disabled) {
                DebugLogger.INSTANCE.info(TAG, "Will not insert into content provider as it is disabled");
                return null;
            }
        }
        DebugLogger.INSTANCE.info(TAG, "Received insert request with uri: %s, values: %s", uri, values);
        if (values != null) {
            switch (uriMatcher.match(uri)) {
                case PRELOAD_INFO_LIST:
                    handleValues(
                            ContentProviderHelperFactory.createPreloadInfoHelper(),
                            values
                    );
                    break;
                case CLIDS:
                    handleValues(
                            ContentProviderHelperFactory.createClidsInfoHelper(),
                            values
                    );
                    break;
                default:
                    SdkUtils.logAttribution("Bad content provider uri.");
                    DebugLogger.INSTANCE.info(TAG, "Bad uri: " + uri);
                    break;
            }
        }
        ContentProviderFirstLaunchHelper.onInsertFinished();
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SdkUtils.logAttribution("Deleting is not supported");
        return -1;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SdkUtils.logAttribution("Updating is not supported");
        return -1;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SdkUtils.logAttribution("Query is not supported");
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    public synchronized void disable() {
        disabled = true;
    }

    @Nullable
    private Context getApplicationContext() {
        final Context context = getContext();
        return context == null ? null : context.getApplicationContext();
    }

    private void handleValues(@NonNull ContentProviderHelper<?> helper, @NonNull ContentValues values) {
        final Context context = getApplicationContext();
        if (context != null) {
            helper.handle(context, values);
        }
    }
}
