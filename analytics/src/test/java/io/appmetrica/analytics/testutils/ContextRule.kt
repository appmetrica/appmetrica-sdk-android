package io.appmetrica.analytics.testutils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.DisplayMetrics
import org.junit.rules.ExternalResource
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.reflect.KProperty

class ContextRule : ExternalResource() {

    lateinit var context: Context
        private set
    lateinit var resources: Resources
        private set
    lateinit var sharedPreferences: SharedPreferences
        private set
    lateinit var packageManager: PackageManager
        private set
    var filesDir: File? = null
        private set
    var noBackupDir: File? = null
        private set
    var databaseDir: File? = null
        private set

    var dataDir: File? = null
        private set

    public override fun before() {
        filesDir = createTempDirectory("files_dir").toFile()
        noBackupDir = createTempDirectory("no_backup_dir").toFile()
        databaseDir = createTempDirectory("database_dir").toFile()
        dataDir = createTempDirectory("data").toFile()
        val displayMetrics = mock<DisplayMetrics>()
        resources = mock<Resources> {
            on { getDisplayMetrics() } doReturn displayMetrics
        }
        sharedPreferences = mock<SharedPreferences>()
        packageManager = mock<PackageManager>()
        context = mock {
            on { packageName } doReturn PACKAGE_NAME
            on { applicationContext } doReturn this@mock.mock
            on { resources } doReturn resources
            on { filesDir } doReturn filesDir
            on { noBackupFilesDir } doReturn noBackupDir
            on { dataDir } doReturn dataDir
            on { getSharedPreferences(any(), any()) } doReturn sharedPreferences
            on { getDatabasePath(any()) } doAnswer {
                val dbName = it.arguments.first() as String
                File(databaseDir, dbName)
            }
            on { packageManager } doReturn packageManager
        }
    }

    public override fun after() {
        filesDir?.deleteRecursively()
        noBackupDir?.deleteRecursively()
        databaseDir?.deleteRecursively()
        dataDir?.deleteRecursively()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Context {
        return context
    }

    companion object {
        const val PACKAGE_NAME = "io.appmetrica.analytics.test"
    }
}
