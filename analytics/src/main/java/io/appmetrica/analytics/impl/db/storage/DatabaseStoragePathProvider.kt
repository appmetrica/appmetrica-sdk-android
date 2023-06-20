package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils.move
import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import java.io.File

internal class DatabaseStoragePathProvider(
    private val targetDirProvider: DatabaseFullPathProvider,
    private val possibleOldDirProviders: List<DatabaseFullPathProvider>,
    tagPostfix: String
) {

    private val tag = "[DatabaseStoragePathProvider-$tagPostfix]"

    fun getPath(context: Context, databaseNameProvider: DatabaseSimpleNameProvider): String {
        try {
            val actualDb = targetDirProvider.fullPath(context, databaseNameProvider.databaseName)
            YLogger.info(tag, "actual db path = ${actualDb.path}")

            if (!actualDb.exists()) {
                YLogger.info(tag, "Db for name `${databaseNameProvider.databaseName}` does not exist.")

                val dbParentDir = actualDb.parentFile
                if (dbParentDir == null) {
                    YLogger.info(
                        tag,
                        "Parent directory for db = `${databaseNameProvider.databaseName}` is null."
                    )
                } else {
                    var shouldMigrate = true
                    if (!dbParentDir.exists()) {
                        YLogger.info(tag, "Parent directory $dbParentDir does not exist. Trying to create...")
                        val status = dbParentDir.mkdirs()
                        YLogger.info(tag, "Parent directory $dbParentDir creating status: $status")
                        if (!status) {
                            YLogger.info(tag, "Couldn't create directory: $dbParentDir")
                            shouldMigrate = false
                        }
                    }
                    if (shouldMigrate) {
                        YLogger.info(tag, "Import db ${actualDb.path} from old storages...")
                        importFromOldStorages(context, databaseNameProvider.legacyDatabaseName, actualDb)
                    }
                }
            } else {
                YLogger.info(
                    tag,
                    "Db for name `${databaseNameProvider.databaseName}` exists. " +
                        "Actual path = `${actualDb.path}`"
                )
            }
            return actualDb.path
        } catch (e: Throwable) {
            YLogger.error(tag, e)
        }

        return databaseNameProvider.databaseName
    }

    private fun importFromOldStorages(context: Context, oldDbName: String, targetDbFile: File) {
        YLogger.info(tag, "Import targetDb = `${targetDbFile.path}` from legacy $oldDbName")
        possibleOldDirProviders.any {
            importFromOldStorage(it.fullPath(context, oldDbName), targetDbFile)
        }
    }

    private fun importFromOldStorage(oldDbFile: File, targetDbFile: File): Boolean {
        if (!oldDbFile.exists()) {
            YLogger.info(tag, "Old db `${oldDbFile.path}` does not exist")
        } else {
            try {
                YLogger.info(tag, "Old db `${oldDbFile.path} exists`. Importing...")
                move(oldDbFile, targetDbFile)

                val oldDbFilePath = oldDbFile.path
                val targetDbFilePath = targetDbFile.path

                YLogger.info(tag, "Move journal files for db: ${oldDbFile.path}...")
                listOf("-journal", "-shm", "-wal").forEach {
                    move(File(oldDbFilePath + it), File(targetDbFilePath + it))
                }
                return true
            } catch (e: Throwable) {
                YLogger.error(tag, e)
            }
        }
        return false
    }

    private fun move(from: File, to: File) {
        val status = from.move(to)
        YLogger.info(tag, "Move $from -> $to with status $status")
    }
}
