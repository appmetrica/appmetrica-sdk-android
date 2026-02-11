package io.appmetrica.analytics.impl.id.reflection

import android.os.Bundle
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

private const val PROVIDER = "io.appmetrica.analytics.identifiers.extra.PROVIDER"
private const val ID = "io.appmetrica.analytics.identifiers.extra.ID"
private const val LIMITED = "io.appmetrica.analytics.identifiers.extra.LIMITED"

private const val TRACKING_INFO = "io.appmetrica.analytics.identifiers.extra.TRACKING_INFO"
private const val STATUS = "io.appmetrica.analytics.identifiers.extra.STATUS"
private const val ERROR_MESSAGE = "io.appmetrica.analytics.identifiers.extra.ERROR_MESSAGE"

private const val YANDEX = "yandex"

internal class ReflectionAdvIdParserTest : CommonTest() {

    private val parser = ReflectionAdvIdParser()

    @Test
    fun emptyBundle() {
        assertThat(parser.fromBundle(null)).isNull()
    }

    @Test
    fun noTrackingData() {
        val errorMessage = "some message"
        val bundle = mock<Bundle> {
            on { getString(STATUS) } doReturn "OK"
            on { getString(ERROR_MESSAGE) } doReturn errorMessage
            on { getBundle(TRACKING_INFO) } doReturn null
        }
        val data = parser.fromBundle(bundle)

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

        val trackingInfoBundle = mock<Bundle> {
            on { getString(PROVIDER) } doReturn YANDEX
            on { getString(ID) } doReturn id
            on { getBoolean(LIMITED) } doReturn limited
            on { containsKey(LIMITED) } doReturn true
        }

        val bundle = mock<Bundle> {
            on { getString(STATUS) } doReturn "OK"
            on { getString(ERROR_MESSAGE) } doReturn errorMessage
            on { getBundle(TRACKING_INFO) } doReturn trackingInfoBundle
        }

        val data = parser.fromBundle(bundle)

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

        val trackingInfoBundle = mock<Bundle> {
            on { getString(PROVIDER) } doReturn YANDEX
            on { getString(ID) } doReturn id
            on { containsKey(LIMITED) } doReturn false
        }

        val bundle = mock<Bundle> {
            on { getString(STATUS) } doReturn "OK"
            on { getString(ERROR_MESSAGE) } doReturn null
            on { getBundle(TRACKING_INFO) } doReturn trackingInfoBundle
        }

        val data = parser.fromBundle(bundle)

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

        val trackingInfoBundle = mock<Bundle> {
            on { getString(PROVIDER) } doReturn "test"
            on { getString(ID) } doReturn id
            on { containsKey(LIMITED) } doReturn false
        }

        val bundle = mock<Bundle> {
            on { getString(STATUS) } doReturn "OK"
            on { getBundle(TRACKING_INFO) } doReturn trackingInfoBundle
        }

        parser.fromBundle(bundle)
    }
}
