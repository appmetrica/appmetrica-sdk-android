package io.appmetrica.analytics.impl.referrer.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class HuaweiReferrerRetriever {

    private static final String TAG = "[HuaweiReferrerRetriever]";

    private static final String REFERRER_PROVIDER_URI = "content://com.huawei.appmarket.commondata/item/5";
    private static final int INDEX_REFERRER = 0;
    private static final int INDEX_CLICK_DOWNLOAD = 1;
    private static final int INDEX_INSTALL_TIME = 2;
    private final static int FUTURE_TIMEOUT_SECONDS = 5;
    @NonNull
    private final Context context;
    @Nullable
    private Cursor cursor;

    public HuaweiReferrerRetriever(@NonNull Context context) {
        this.context = context;
    }

    @WorkerThread
    public void retrieveReferrer(@NonNull ReferrerReceivedListener referrerListener) {
        ReferrerInfo resultReferrerInfo = null;
        try {
            FutureTask<ReferrerInfo> referrerInfoFuture = new FutureTask<ReferrerInfo>(createReferrerInfoCallable());
            GlobalServiceLocator.getInstance().getServiceExecutorProvider()
                    .getHmsReferrerThread(referrerInfoFuture).start();
            resultReferrerInfo = referrerInfoFuture.get(FUTURE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex, "Could not retrieve Huawei referrer");
            referrerListener.onReferrerRetrieveError(ex);
            return;
        } finally {
            Utils.closeCursor(cursor);
        }
        referrerListener.onReferrerReceived(resultReferrerInfo);
    }

    @NonNull
    private Callable<ReferrerInfo> createReferrerInfoCallable() {
        return new Callable<ReferrerInfo>() {
            @Override
            public ReferrerInfo call() throws Exception {
                ReferrerInfo referrerInfo = null;
                Uri uri = Uri.parse(REFERRER_PROVIDER_URI);
                final ContentResolver contentResolver = context.getContentResolver();
                cursor = contentResolver.query(uri, null, null, new String[]{ context.getPackageName() }, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        String referrer = cursor.getString(INDEX_REFERRER);
                        if (TextUtils.isEmpty(referrer) == false) {
                            long clickTimestamp = cursor.getLong(INDEX_CLICK_DOWNLOAD);
                            long installTimestamp = cursor.getLong(INDEX_INSTALL_TIME);
                            referrerInfo = new ReferrerInfo(
                                    referrer,
                                    clickTimestamp,
                                    installTimestamp,
                                    ReferrerInfo.Source.HMS
                            );
                            DebugLogger.info(TAG, "Parsed referrer: %s", referrerInfo);
                        } else {
                            DebugLogger.info(TAG, "Referrer is empty");
                        }
                    } else {
                        DebugLogger.info(TAG, "Empty cursor");
                    }
                } else {
                    DebugLogger.info(TAG, "No content provider found");
                }
                return referrerInfo;
            }
        };
    }
}
