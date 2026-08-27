package io.appmetrica.analytics.impl.clids

import android.content.Context
import io.appmetrica.analytics.impl.ContentProviderFirstLaunchHelper
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedStaticRule
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock

internal class ClidsDataAwaiterTest : CommonTest() {

    private val context = mock<Context>()

    @get:Rule
    val contentProviderFirstLaunchHelper = MockedStaticRule(ContentProviderFirstLaunchHelper::class.java)
    private val dataWaiter = ClidsDataAwaiter()

    @Test
    fun waitForData() {
        dataWaiter.waitForData(context)
        contentProviderFirstLaunchHelper.staticMock.verify {
            ContentProviderFirstLaunchHelper.awaitContentProviderWarmUp(context)
        }
    }
}
