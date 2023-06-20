package io.appmetrica.analytics.coreutils.internal.io

import android.content.Context
import android.os.Build
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import java.io.File

@Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
class AppStorageDirectoryProviderForLollipopTest {

    private val noBackupDir = mock<File>()

    private val context = mock<Context> {
        on { noBackupFilesDir } doReturn noBackupDir
    }

    @Test
    fun getAppStorageDirectory() {
        assertThat(AppStorageDirectoryProviderForLollipop.getAppStorageDirectory(context)).isEqualTo(noBackupDir)
    }
}
