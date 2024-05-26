package io.appmetrica.analytics.impl.db.storage

import io.appmetrica.analytics.logger.internal.DebugLogger
import java.io.File

internal class DatabaseStoragePathProviderFactory(
    outerStorageDirectory: File?
) {

    private val tag = "[DatabaseStoragePathProviderFactory]"

    private val targetDirProvider: DatabaseFullPathProvider
    private val possibleOldDatabaseDirProviders = ArrayList<DatabaseFullPathProvider>()

    init {
        if (outerStorageDirectory != null) {
            DebugLogger.info(tag, "Init for lollipop with outer database directory")
            targetDirProvider =
                OuterRootDatabaseFullPathProvider(outerStorageDirectory, DatabaseRelativePathFormer())
            possibleOldDatabaseDirProviders.add(
                OuterRootDatabaseFullPathProvider(outerStorageDirectory, OldDatabaseRelativePathFormer())
            )
        } else {
            DebugLogger.info(tag, "Init for lollipop without outer database directory")
            targetDirProvider = DatabaseFullPathProviderImpl(DatabaseRelativePathFormer())
        }
        possibleOldDatabaseDirProviders.add(DatabaseFullPathProviderImpl(OldDatabaseRelativePathFormer()))
    }

    fun create(tagPostfix: String, doNotDeleteSourceFile: Boolean): DatabaseStoragePathProvider =
        DatabaseStoragePathProvider(
            targetDirProvider,
            possibleOldDatabaseDirProviders,
            doNotDeleteSourceFile,
            tagPostfix
        )
}
