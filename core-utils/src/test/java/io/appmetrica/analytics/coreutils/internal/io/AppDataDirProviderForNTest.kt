package io.appmetrica.analytics.coreutils.internal.io

import android.content.Context
import android.os.Build
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config
import java.io.File

@Config(sdk = [Build.VERSION_CODES.N])
class AppDataDirProviderForNTest {

    private val dataDir = mock<File>()

    private val context = mock<Context> {
        on { dataDir } doReturn dataDir
    }

    @Test
    fun data() {
        assertThat(AppDataDirProviderForN.dataDir(context)).isEqualTo(dataDir)
    }
}
