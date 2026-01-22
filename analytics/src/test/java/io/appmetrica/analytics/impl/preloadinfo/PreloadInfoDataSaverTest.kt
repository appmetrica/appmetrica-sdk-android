package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class PreloadInfoDataSaverTest : CommonTest() {

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val dataSaver = PreloadInfoDataSaver()

    @Test
    fun saveData() {
        val data = mock<PreloadInfoState>()
        dataSaver(data)
        val preloadInfoStorage = GlobalServiceLocator.getInstance().preloadInfoStorage
        verify(preloadInfoStorage).updateIfNeeded(data)
    }
}
