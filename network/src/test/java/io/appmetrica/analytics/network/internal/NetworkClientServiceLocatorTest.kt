package io.appmetrica.analytics.network.internal

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NetworkClientServiceLocatorTest : CommonTest() {

    private val metaData = Bundle()

    private val context: Context = mock()

    private val packageManager: SafePackageManager = mock {
        on { getApplicationMetaData(context) } doReturn metaData
    }

    @Test
    fun `init gets application meta data`() {
        NetworkClientServiceLocator.init(context, packageManager)
        assertThat(NetworkClientServiceLocator.getInstance().applicationMetaData).isSameAs(metaData)
    }
}
