package io.appmetrica.analytics.impl.db.storage

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.logger.internal.DebugLogger
import java.io.File

internal class DatabaseStoragePathProviderFactory(
    outerStorageDirectory: File?
) {

    private val tag = "[DatabaseStoragePathProviderFactory]"

    private val targetDirProvider: DatabaseFullPathProvider
    private val possibleOldDatabaseDirProviders = ArrayList<DatabaseFullPathProvider>()

    init {
        if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.LOLLIPOP)) {
            if (outerStorageDirectory != null) {
                DebugLogger.info(tag, "Init for lollipop with outer database directory")
                targetDirProvider =
                    OuterRootDatabaseFullPathProvider(outerStorageDirectory, DatabaseRelativePathFormer())
                possibleOldDatabaseDirProviders.add(
                    OuterRootDatabaseFullPathProvider(outerStorageDirectory, OldDatabaseRelativePathFormer())
                )
            } else {
                DebugLogger.info(tag, "Init for lollipop without outer database directory")
                targetDirProvider = LollipopDatabaseFullPathProvider(DatabaseRelativePathFormer())
            }
            possibleOldDatabaseDirProviders.add(LollipopDatabaseFullPathProvider(OldDatabaseRelativePathFormer()))
        } else {
            DebugLogger.info(tag, "Init for pre-lollipop")
            targetDirProvider = PreLollipopDatabaseFullPathProvider(PreLollipopRelativePathFormer())
            possibleOldDatabaseDirProviders.add(PreLollipopDatabaseFullPathProvider(OldDatabaseRelativePathFormer()))
        }
    }

    fun create(tagPostfix: String, doNotDeleteSourceFile: Boolean): DatabaseStoragePathProvider =
        DatabaseStoragePathProvider(
            targetDirProvider,
            possibleOldDatabaseDirProviders,
            doNotDeleteSourceFile,
            tagPostfix
        )
}
