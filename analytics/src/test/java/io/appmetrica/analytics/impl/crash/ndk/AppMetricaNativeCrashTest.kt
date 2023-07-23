package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AppMetricaNativeCrashTest : CommonTest() {
    companion object {
        private val DEFAULT_SOURCE = NativeCrashSource.CRASHPAD
        private const val DEFAULT_HANDLER_VERSION = "someVersion"
        private const val DEFAULT_UUID = "uuid"
        private const val DEFAULT_DUMP_FILE = "dumpFile"
        private const val DEFAULT_CREATION_TIME = 1L
        private const val DEFAULT_METADATA = "metadata"
    }

    private val metadata = mock<AppMetricaNativeCrashMetadata>()

    @get:Rule
    val appMetricaNativeCrashMetadataSerializerMockedConstructionRule =
        constructionRule<AppMetricaNativeCrashMetadataSerializer> {
            on { deserialize(anyString()) } doReturn null
            on { deserialize(DEFAULT_METADATA) } doReturn metadata
        }

    @Test
    fun `from NativeCrash`() {
        val crash = AppMetricaNativeCrash.from(createNativeCrash())

        assertThat(crash).isNotNull()
        assertThat(crash!!.source).isEqualTo(DEFAULT_SOURCE)
        assertThat(crash.handlerVersion).isEqualTo(DEFAULT_HANDLER_VERSION)
        assertThat(crash.uuid).isEqualTo(DEFAULT_UUID)
        assertThat(crash.dumpFile).isEqualTo(DEFAULT_DUMP_FILE)
        assertThat(crash.creationTime).isEqualTo(DEFAULT_CREATION_TIME)
        assertThat(crash.metadata).isEqualTo(metadata)
    }

    @Test
    fun `from NativeCrash with broken metadata`() {
        val crash = AppMetricaNativeCrash.from(createNativeCrash(metadata = "broken metadata"))

        assertThat(crash).isNull()
    }

    private fun createNativeCrash(metadata: String = DEFAULT_METADATA): NativeCrash = NativeCrash.Builder(
        DEFAULT_SOURCE,
        DEFAULT_HANDLER_VERSION,
        DEFAULT_UUID,
        DEFAULT_DUMP_FILE,
        DEFAULT_CREATION_TIME,
        metadata
    ).build()
}
