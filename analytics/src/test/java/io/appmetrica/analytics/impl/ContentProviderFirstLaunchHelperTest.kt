package io.appmetrica.analytics.impl

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import io.appmetrica.analytics.internal.PreloadInfoContentProvider
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ContentProviderFirstLaunchHelperTest : CommonTest() {

    private val timeout = 1000
    private val packageName = "test.package"
    private val contentProvider = mock<PreloadInfoContentProvider>()
    private val packageManager = mock<PackageManager>()
    private val context = mock<Context> {
        on { packageManager } doReturn packageManager
        on { packageName } doReturn packageName
    }

    @Test
    fun createInsertAndWait() {
        ContentProviderFirstLaunchHelper.onCreate(contentProvider)
        ContentProviderFirstLaunchHelper.onInsertFinished()
        checkTime({ it < timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyContentProviderDisabled()
    }

    @Test
    fun createAndWait() {
        ContentProviderFirstLaunchHelper.onCreate(contentProvider)
        checkTime({ it >= timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyContentProviderDisabled()
    }

    @Test
    fun insertAndWaitWithoutCreate() {
        ContentProviderFirstLaunchHelper.onInsertFinished()
        checkTime({ it < timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyNoInteractions(packageManager)
    }

    @Test
    fun createInsertAndWaitAndAgainWait() {
        ContentProviderFirstLaunchHelper.onCreate(contentProvider)
        ContentProviderFirstLaunchHelper.onInsertFinished()
        checkTime({ it < timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyContentProviderDisabled()

        clearInvocations(packageManager, contentProvider)
        checkTime({ it < timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyNoInteractions(packageManager, contentProvider)
    }

    @Test
    fun createInsertAndWaitAndAgainInsert() {
        ContentProviderFirstLaunchHelper.onCreate(contentProvider)
        ContentProviderFirstLaunchHelper.onInsertFinished()
        checkTime({ it < timeout }) {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
        verifyContentProviderDisabled()

        clearInvocations(packageManager, contentProvider)
        ContentProviderFirstLaunchHelper.onInsertFinished()
    }

    private fun checkTime(timeCondition: (Long) -> Boolean, block: () -> Unit) {
        val start = System.currentTimeMillis()
        block()
        val end = System.currentTimeMillis()
        assertThat(timeCondition(end - start)).isTrue
    }

    private fun verifyContentProviderDisabled() {
        verify(packageManager).setComponentEnabledSetting(
            argThat { component: ComponentName ->
                component.className == PreloadInfoContentProvider::class.java.name &&
                    component.packageName == packageName
            },
            eq(PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
            eq(PackageManager.DONT_KILL_APP)
        )
        verify(contentProvider).disable()
    }
}
