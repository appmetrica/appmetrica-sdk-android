package io.appmetrica.analytics.impl.startup.uuid;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class MultiProcessSafeUuidProvider {

    private static final String TAG = "[MultiProcessSafeUuidProvider]";

    @NonNull
    private final Context context;
    @NonNull
    private final IOuterSourceUuidImporter outerSourceUuidImporter;
    @NonNull
    private final ExclusiveMultiProcessFileLock lock;
    @NonNull
    private final PersistentUuidHolder persistentUuidHolder;
    @NonNull
    private final UuidValidator uuidValidator;
    @Nullable
    private volatile IdentifiersResult uuidResult;

    public MultiProcessSafeUuidProvider(@NonNull Context context,
                                        @NonNull IOuterSourceUuidImporter outerSourceUuidImporter) {
        this(
            context,
            outerSourceUuidImporter,
            MultiProcessUuidLockProvider.getLock(context),
            new PersistentUuidHolder(context),
            new UuidValidator()
        );
    }

    @VisibleForTesting
    MultiProcessSafeUuidProvider(@NonNull Context context,
                                 @NonNull IOuterSourceUuidImporter outerSourceUuidImporter,
                                 @NonNull ExclusiveMultiProcessFileLock lock,
                                 @NonNull PersistentUuidHolder persistentUuidHolder,
                                 @NonNull UuidValidator uuidValidator) {

        this.context = context;
        this.outerSourceUuidImporter = outerSourceUuidImporter;
        this.lock = lock;
        this.persistentUuidHolder = persistentUuidHolder;
        this.uuidValidator = uuidValidator;
        try {
            this.lock.lock();
            this.persistentUuidHolder.checkMigration();
        } catch (Throwable throwable) {
            DebugLogger.INSTANCE.error(TAG, throwable);
        } finally {
            this.lock.unlock();
        }
    }

    @NonNull
    public IdentifiersResult readUuid() {
        IdentifiersResult localUuidResult = uuidResult;
        if (isOk(localUuidResult)) {
            DebugLogger.INSTANCE.info(TAG, "Return valid uuid result from memory state");
            return localUuidResult;
        }
        try {
            DebugLogger.INSTANCE.info(TAG, "Acquire file lock: uuid.data.lock");
            lock.lock();
            localUuidResult = uuidResult;
            if (!isOk(localUuidResult)) {
                String resultUuid;
                resultUuid = persistentUuidHolder.readUuid();
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Current uuid in existing uuid storage after acquiring lock = %s",
                    resultUuid
                );
                if (!uuidValidator.isValid(resultUuid)) {
                    resultUuid = outerSourceUuidImporter.get(context);
                    DebugLogger.INSTANCE.info(TAG, "Uuid from outer importer = %s", resultUuid);
                    resultUuid = persistentUuidHolder.handleUuid(resultUuid);
                }
                if (uuidValidator.isValid(resultUuid)) {
                    localUuidResult = new IdentifiersResult(resultUuid, IdentifierStatus.OK, null);
                    uuidResult = localUuidResult;
                }
            }
        } catch (Throwable throwable) {
            DebugLogger.INSTANCE.error(TAG, throwable);
        } finally {
            DebugLogger.INSTANCE.info(TAG, "Clear file lock: uuid.data.lock");
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
        return result != null && result.status == IdentifierStatus.OK && uuidValidator.isValid(result.id);
    }
}
