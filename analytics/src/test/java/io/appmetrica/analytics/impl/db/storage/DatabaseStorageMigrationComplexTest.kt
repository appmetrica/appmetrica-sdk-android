package io.appmetrica.analytics.impl.db.storage

import android.content.Context
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class DatabaseStorageMigrationComplexTest(
    private val description: String,
    private val doNotDeleteSourceFile: Boolean,
    private val outerDatabaseDirPath: String?,
    private val oldFilesPaths: List<String>,
    private val expectedPath: String,
    private val expectedExistsFilesPaths: List<String>,
    private val expectedNotExistsFilesPaths: List<String>
) : CommonTest() {

    companion object {

        private val outerDir = "outer_dir"
        private val oldFileName = "old.db"
        private val actualFileName = "actual.db"

        @ParameterizedRobolectricTestRunner.Parameters(name = "{index} - {0}")
        @JvmStatic
        fun data(): List<Array<Any?>> = listOf(
            // Without outer storage
            arrayOf(
                "Without outer dir and with actual database exists",
                false,
                null,
                listOf(
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal",
                    "no_backup/appmetrica/analytics/db/$actualFileName"
                ),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName"
                ),
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "Without outer dir and old databases in no_backup directory",
                false,
                null,
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName",
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal"
                ),
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
            ),
            arrayOf(
                "Without outer dir and old databases in no_backup directory with doNotDeleteSourceFile",
                true,
                null,
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName",
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal",
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal"
                ),
                listOf<String>(),
            ),
            arrayOf(
                "Without outer dir and without old databases in no_backup directory",
                false,
                null,
                listOf<String>(),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName",
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "Without outer dir and only main db file in no_backup directory",
                false,
                null,
                listOf("no_backup/$oldFileName"),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName"
                ),
                listOf("no_backup/$oldFileName"),
            ),
            arrayOf(
                "Without outer dir and only main db file in no_backup directory with doNotDeleteSourceFile",
                true,
                null,
                listOf("no_backup/$oldFileName"),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName",
                    "no_backup/$oldFileName"
                ),
                listOf<String>(),
            ),
            arrayOf(
                "Without outer dir and only journal file in database directory",
                false,
                null,
                listOf("no_backup/$oldFileName-journal"),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf("no_backup/appmetrica/analytics/db/$actualFileName-journal"),
            ),
            arrayOf(
                "Without outer dir and odd files in no_backup directory",
                false,
                null,
                listOf(
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal",
                    "no_backup/$oldFileName-shm", "no_backup/$oldFileName-wal",
                    "no_backup/$oldFileName-journal2", "no_backup/$oldFileName-shm2", "no_backup/$oldFileName-wal2"
                ),
                "no_backup/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "no_backup/appmetrica/analytics/db/$actualFileName",
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal",
                    "no_backup/appmetrica/analytics/db/$actualFileName-shm",
                    "no_backup/appmetrica/analytics/db/$actualFileName-wal"
                ),
                listOf(
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal", "no_backup/$oldFileName-shm",
                    "no_backup/$oldFileName-wal",
                    "no_backup/appmetrica/analytics/db/$actualFileName-journal2",
                    "no_backup/appmetrica/analytics/db/$actualFileName-shm2",
                    "no_backup/appmetrica/analytics/db/$actualFileName-wal2"
                ),
            ),
            // With outer storage
            arrayOf(
                "With outer dir and with actual database exists if old database in no_backup exists too",
                false,
                outerDir,
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal"
                ),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName"
                ),
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "With outer dir and with actual database exists if old database in outer storagr exists too",
                false,
                outerDir,
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/$oldFileName", "$outerDir/$oldFileName-journal"
                ),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName"
                ),
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "With outer dir and old databases in no_backup directory",
                false,
                outerDir,
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal"
                ),
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
            ),
            arrayOf(
                "With outer dir and old databases in no_backup directory with doNotDeleteSourceFile",
                true,
                outerDir,
                listOf("no_backup/$oldFileName", "no_backup/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal",
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal"
                ),
                listOf<String>(),
            ),
            arrayOf(
                "With outer dir and old databases in no_backup directory",
                false,
                outerDir,
                listOf("$outerDir/$oldFileName", "$outerDir/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal"
                ),
                listOf("$outerDir/$oldFileName", "$outerDir/$oldFileName-journal"),
            ),
            arrayOf(
                "With outer dir and old databases in no_backup directory with doNotDeleteSourceFile",
                true,
                outerDir,
                listOf("$outerDir/$oldFileName", "$outerDir/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal",
                    "$outerDir/$oldFileName", "$outerDir/$oldFileName-journal"
                ),
                listOf<String>(),
            ),
            arrayOf(
                "With outer dir and without old databases",
                false,
                outerDir,
                listOf<String>(),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "With outer dir and without old databases in no_backup directory",
                false,
                outerDir,
                listOf<String>(),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/no_backup/appmetrica/analytics/db/$actualFileName-journal"
                ),
            ),
            arrayOf(
                "With outer dir and only main db file in no_backup directory",
                false,
                outerDir,
                listOf("no_backup/$oldFileName"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName"
                ),
                listOf("no_backup/$oldFileName"),
            ),
            arrayOf(
                "With outer dir and only main db file in outerDir directory",
                false,
                outerDir,
                listOf("$outerDir/$oldFileName"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName"
                ),
                listOf("$outerDir/$oldFileName"),
            ),
            arrayOf(
                "With outer dir and only journal file in database directory",
                false,
                outerDir,
                listOf("no_backup/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf("$outerDir/appmetrica/analytics/db/$actualFileName-journal"),
            ),
            arrayOf(
                "With outer dir and only journal file in database directory",
                false,
                outerDir,
                listOf("$outerDir/$oldFileName-journal"),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf<String>(),
                listOf("$outerDir/appmetrica/analytics/db/$actualFileName-journal"),
            ),
            arrayOf(
                "With outer dir and odd files in no_backup directory",
                false,
                outerDir,
                listOf(
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal",
                    "no_backup/$oldFileName-shm", "no_backup/$oldFileName-wal",
                    "no_backup/$oldFileName-journal2", "no_backup/$oldFileName-shm2", "no_backup/$oldFileName-wal2"
                ),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-shm",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-wal"
                ),
                listOf(
                    "no_backup/$oldFileName", "no_backup/$oldFileName-journal", "no_backup/$oldFileName-shm",
                    "no_backup/$oldFileName-wal",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal2",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-shm2",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-wal2"
                ),
            ),
            arrayOf(
                "With outer dir and odd files in no_backup directory",
                false,
                outerDir,
                listOf(
                    "$outerDir/$oldFileName", "$outerDir/$oldFileName-journal",
                    "$outerDir/$oldFileName-shm", "$outerDir/$oldFileName-wal",
                    "$outerDir/$oldFileName-journal2", "$outerDir/$oldFileName-shm2", "$outerDir/$oldFileName-wal2"
                ),
                "$outerDir/appmetrica/analytics/db/$actualFileName",
                listOf(
                    "$outerDir/appmetrica/analytics/db/$actualFileName",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-shm",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-wal"
                ),
                listOf(
                    "$outerDir/$oldFileName", "$outerDir/$oldFileName-journal", "$outerDir/$oldFileName-shm",
                    "$outerDir/$oldFileName-wal",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-journal2",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-shm2",
                    "$outerDir/appmetrica/analytics/db/$actualFileName-wal2"
                ),
            ),
        )
    }

    @get:Rule
    val logRule = LogRule()

    private lateinit var context: Context
    private lateinit var databaseStoragePathProviderFactory: DatabaseStoragePathProviderFactory
    private lateinit var databaseStoragePathProvider: DatabaseStoragePathProvider
    private lateinit var databaseSimpleNameProvider: DatabaseSimpleNameProvider
    private lateinit var dataDir: File

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        dataDir = context.filesDir.parentFile!!
        File(dataDir, "databases").mkdirs()
        File(dataDir, outerDir).mkdirs()
        val noBackup = context.noBackupFilesDir
        if (!noBackup.exists()) {
            noBackup.mkdirs()
        }
        // Just for one test
        if (oldFilesPaths.contains("no_backup/appmetrica/analytics/db/$actualFileName")) {
            File(noBackup, "/appmetrica/analytics/db/").mkdirs()
        }
        if (oldFilesPaths.contains("$outerDir/appmetrica/analytics/db/$actualFileName")) {
            File(dataDir, "$outerDir/appmetrica/analytics/db/").mkdirs()
        }
        oldFilesPaths.forEach {
            File(dataDir, it).writeText("Some text for file: $it")
        }
        databaseStoragePathProviderFactory =
            DatabaseStoragePathProviderFactory(outerDatabaseDirPath?.let { File(dataDir, it) })
        databaseStoragePathProvider = databaseStoragePathProviderFactory.create("Some tag", doNotDeleteSourceFile)
        databaseSimpleNameProvider = object : DatabaseSimpleNameProvider {
            override val databaseName: String = actualFileName
            override val legacyDatabaseName: String = oldFileName
        }
    }

    @Test
    fun checkFiles() {
        assertThat(databaseStoragePathProvider.getPath(context, databaseSimpleNameProvider))
            .isEqualTo(File(dataDir, expectedPath).path)
        expectedExistsFilesPaths.forEach {
            assertThat(File(dataDir, it)).describedAs(it).exists()
        }
        expectedNotExistsFilesPaths.forEach {
            assertThat(File(dataDir, it)).describedAs(it).doesNotExist()
        }
    }
}
