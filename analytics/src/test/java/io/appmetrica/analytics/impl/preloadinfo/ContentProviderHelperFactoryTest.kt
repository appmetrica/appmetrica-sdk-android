package io.appmetrica.analytics.impl.preloadinfo

import io.appmetrica.analytics.impl.ContentProviderHelper
import io.appmetrica.analytics.impl.clids.ClidsDataParser
import io.appmetrica.analytics.impl.clids.ClidsDataSaver
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.SoftAssertions
import org.junit.Rule
import org.junit.Test

internal class ContentProviderHelperFactoryTest : CommonTest() {

    @Rule
    @JvmField
    val sContentProviderHelper = MockedConstructionRule(ContentProviderHelper::class.java)

    @Test
    fun createPreloadInfoHelper() {
        ContentProviderHelperFactory.createPreloadInfoHelper()
        val arguments = sContentProviderHelper.argumentInterceptor.flatArguments()
        val softly = SoftAssertions()
        softly.assertThat(arguments.size).isEqualTo(3)
        softly.assertThat(arguments[0]).isExactlyInstanceOf(PreloadInfoDataParser::class.java)
        softly.assertThat(arguments[1]).isExactlyInstanceOf(PreloadInfoDataSaver::class.java)
        softly.assertThat(arguments[2]).isEqualTo("preload info")
        softly.assertAll()
    }

    @Test
    fun createClidsInfoHelper() {
        ContentProviderHelperFactory.createClidsInfoHelper()
        val arguments = sContentProviderHelper.argumentInterceptor.flatArguments()
        val softly = SoftAssertions()
        softly.assertThat(arguments.size).isEqualTo(3)
        softly.assertThat(arguments[0]).isExactlyInstanceOf(ClidsDataParser::class.java)
        softly.assertThat(arguments[1]).isExactlyInstanceOf(ClidsDataSaver::class.java)
        softly.assertThat(arguments[2]).isEqualTo("clids")
        softly.assertAll()
    }
}
