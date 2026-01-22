package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.BackgroundRestrictionsState.AppStandByBucket
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal abstract class AppStandbyBucketConverterBaseTest(
    private val input: Int,
    private val expected: AppStandByBucket?,
    private val expectedString: String?
) : CommonTest() {

    private val converter by setUp { AppStandbyBucketConverter() }

    @Test
    open fun fromSystemValue() {
        assertThat(converter.fromIntToAppStandbyBucket(input)).isEqualTo(expected)
    }

    @Test
    open fun toStringValue() {
        assertThat(converter.fromAppStandbyBucketToString(expected)).isEqualTo(expectedString)
    }

    companion object {
        const val USAGE_STATE_MANAGER_APP_STAND_BY_BUCKET_EXEMPTED = 5
        const val BUCKET_EXEMPTED = "EXEMPTED"
        const val BUCKET_ACTIVE = "ACTIVE"
        const val BUCKET_WORKING_SET = "WORKING_SET"
        const val BUCKET_FREQUENT = "FREQUENT"
        const val BUCKET_RARE = "RARE"
        const val BUCKET_RESTRICTED = "RESTRICTED"
    }
}
