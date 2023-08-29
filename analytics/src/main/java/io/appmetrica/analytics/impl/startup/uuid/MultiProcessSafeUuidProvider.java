package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import kotlin.jvm.Volatile;

public class MultiProcessSafeUuidProvider {

    private static final String TAG = "[UuidProcessSafeProvider]";

    @NonNull
    private final Context context;
    @NonNull
    private final IOuterSourceUuidImporter outerSourceUuidImporter;
    @NonNull
    private final ExclusiveMultiProcessFileLock lock;
    @NonNull
    private final PersistentUuidHolder persistentUuidHolder;
    @Volatile
    @Nullable
    private IdentifiersResult uuidResult;

    public MultiProcessSafeUuidProvider(@NonNull Context context,
                                        @NonNull IOuterSourceUuidImporter outerSourceUuidImporter) {
        this(
            context,
            outerSourceUuidImporter,
            MultiProcessUuidLockProvider.getLock(context),
            new PersistentUuidHolder(context)
        );
    }

    @VisibleForTesting
    MultiProcessSafeUuidProvider(@NonNull Context context,
                                 @NonNull IOuterSourceUuidImporter outerSourceUuidImporter,
                                 @NonNull ExclusiveMultiProcessFileLock lock,
                                 @NonNull PersistentUuidHolder persistentUuidHolder) {

        this.context = context;
        this.outerSourceUuidImporter = outerSourceUuidImporter;
        this.lock = lock;
        this.persistentUuidHolder = persistentUuidHolder;
        try {
            this.lock.lock();
            this.persistentUuidHolder.checkMigration();
        } catch (Throwable throwable) {
            YLogger.error(TAG, throwable);
        } finally {
            this.lock.unlock();
        }
    }

    @NonNull
    public IdentifiersResult readUuid() {
        IdentifiersResult localUuidResult = uuidResult;
        if (isOk(localUuidResult)) {
            YLogger.info(TAG, "Return valid uuid result from memory state");
            return localUuidResult;
        }
        String resultUuid = null;
        try {
            YLogger.info(TAG, "Acquire file lock: uuid.data.lock");
            lock.lock();
            localUuidResult = uuidResult;
            if (!isOk(localUuidResult)) {
                resultUuid = persistentUuidHolder.readUuid();
                YLogger.info(TAG, "Current uuid in existing uuid storage after acquiring lock = %s", resultUuid);
                if (TextUtils.isEmpty(resultUuid)) {
                    resultUuid = outerSourceUuidImporter.get(context);
                    YLogger.info(TAG, "Uuid from outer importer = %s", resultUuid);
                    resultUuid = persistentUuidHolder.handleUuid(resultUuid);
                }
                if (!TextUtils.isEmpty(resultUuid)) {
                    localUuidResult = new IdentifiersResult(resultUuid, IdentifierStatus.OK, null);
                    uuidResult = localUuidResult;
                }
            }
        } catch (Throwable throwable) {
            YLogger.error(TAG, throwable);
        } finally {
            YLogger.info(TAG, "Clear file lock: uuid.data.lock");
            lock.unlock();
        }
        return localUuidResult != null ? localUuidResult : new IdentifiersResult(
            null,
            IdentifierStatus.UNKNOWN,
            "Uuid must be obtained via async API " +
                "AppMetrica#requestStartupParams(Context, StartupParamsCallback, List<String>)"
        );
    }

    private boolean isOk(@Nullable IdentifiersResult result) {
        return result != null && result.status == IdentifierStatus.OK && result.id != null;
    }
}
