package io.appmetrica.analytics.impl.id.reflection

import android.os.Bundle
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val PROVIDER = "io.appmetrica.analytics.identifiers.extra.PROVIDER"
private const val ID = "io.appmetrica.analytics.identifiers.extra.ID"
private const val LIMITED = "io.appmetrica.analytics.identifiers.extra.LIMITED"

private const val TRACKING_INFO = "io.appmetrica.analytics.identifiers.extra.TRACKING_INFO"
private const val STATUS = "io.appmetrica.analytics.identifiers.extra.STATUS"
private const val ERROR_MESSAGE = "io.appmetrica.analytics.identifiers.extra.ERROR_MESSAGE"

private const val GOOGLE = "google"
private const val HUAWEI = "huawei"
private const val YANDEX = "yandex"

@RunWith(RobolectricTestRunner::class)
class ReflectionAdvIdParserTest : CommonTest() {

    private val parser = ReflectionAdvIdParser()

    @Test
    fun emptyBundle() {
        assertThat(parser.fromBundle(null)).isNull()
    }

    @Test
    fun noTrackingData() {
        val errorMessage = "some message"
        val data = parser.fromBundle(
            Bundle().apply {
                putString(STATUS, "OK")
                putString(ERROR_MESSAGE, errorMessage)
            }
        )

        val assert = ObjectPropertyAssertions(data)

        assert.checkField("mAdTrackingInfo", null as AdTrackingInfo?)
        assert.checkField("mStatus", IdentifierStatus.OK)
        assert.checkField("mErrorExplanation", errorMessage)

        assert.checkAll()
    }

    @Test
    fun fineCase() {
        val errorMessage = "some message"
        val id = "someID"
        val limited = true
        val data = parser.fromBundle(
            Bundle().apply {
                putString(STATUS, "OK")
                putString(ERROR_MESSAGE, errorMessage)
                putBundle(
                    TRACKING_INFO,
                    Bundle().apply {
                        putString(PROVIDER, YANDEX)
                        putString(ID, id)
                        putBoolean(LIMITED, limited)
                    }
                )
            }
        )

        val assert = ObjectPropertyAssertions(data)

        assert.checkFieldRecursively("mAdTrackingInfo") { nested: ObjectPropertyAssertions<AdTrackingInfo> ->
            nested.checkField("provider", AdTrackingInfo.Provider.YANDEX)
            nested.checkField("advId", id)
            nested.checkField("limitedAdTracking", limited)

            nested.checkAll()
        }
        assert.checkField("mStatus", IdentifierStatus.OK)
        assert.checkField("mErrorExplanation", errorMessage)

        assert.checkAll()
    }

    @Test
    fun limitedAdTrackingUnknown() {
        val id = "someID"
        val data = parser.fromBundle(
            Bundle().apply {
                putString(STATUS, "OK")
                putBundle(
                    TRACKING_INFO,
                    Bundle().apply {
                        putString(PROVIDER, YANDEX)
                        putString(ID, id)
                    }
                )
            }
        )

        val assert = ObjectPropertyAssertions(data)

        assert.checkFieldRecursively("mAdTrackingInfo") { nested: ObjectPropertyAssertions<AdTrackingInfo> ->
            nested.checkField("provider", AdTrackingInfo.Provider.YANDEX)
            nested.checkField("advId", id)
            nested.checkField("limitedAdTracking", null as Boolean?)

            nested.checkAll()
        }
        assert.checkField("mStatus", IdentifierStatus.OK)
        assert.checkField("mErrorExplanation", null as String?)

        assert.checkAll()
    }

    @Test(expected = IllegalArgumentException::class)
    fun unknownProvider() {
        val id = "someID"
        parser.fromBundle(
            Bundle().apply {
                putString(STATUS, "OK")
                putBundle(
                    TRACKING_INFO,
                    Bundle().apply {
                        putString(PROVIDER, "test")
                        putString(ID, id)
                    }
                )
            }
        )
    }
}
