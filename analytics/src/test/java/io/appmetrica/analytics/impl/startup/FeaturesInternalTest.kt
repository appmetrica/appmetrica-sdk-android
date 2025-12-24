package io.appmetrica.analytics.impl.startup

import android.os.Parcel
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.stubbing
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FeaturesInternalTest : CommonTest() {

    private val parcel = mock<Parcel>()

    @Test
    fun defaultConstructor() {
        val defaultObject = FeaturesInternal()
        ObjectPropertyAssertions(defaultObject)
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun parcelizationFilled() {
        val features = FeaturesInternal(true, IdentifierStatus.UNKNOWN, "some error")
        features.writeToParcel(parcel, 0)
        inOrder(parcel).also {
            it.verify(parcel).writeValue(true)
            it.verify(parcel).writeString("UNKNOWN")
            it.verify(parcel).writeString("some error")
        }
    }

    @Test
    fun parcelizationEmpty() {
        val features = FeaturesInternal()
        features.writeToParcel(parcel, 0)
        inOrder(parcel).also {
            it.verify(parcel).writeValue(null)
            it.verify(parcel).writeString("UNKNOWN")
            it.verify(parcel).writeString(null)
        }
    }

    @Test
    fun deparcelizationFilled() {
        stubbing(parcel) {
            on { readValue(Boolean::class.java.classLoader) } doReturn false
            on { readString() }.doReturn("UNKNOWN", "some error")
        }
        ObjectPropertyAssertions(FeaturesInternal(parcel))
            .checkField("sslPinning", "getSslPinning", false)
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkField("errorExplanation", "getErrorExplanation", "some error")
            .checkAll()
    }

    @Test
    fun deparcelizationEmpty() {
        stubbing(parcel) {
            on { readValue(Boolean::class.java.classLoader) } doReturn null
            on { readString() } doReturn null
        }
        ObjectPropertyAssertions(FeaturesInternal(parcel))
            .checkFieldIsNull("sslPinning", "getSslPinning")
            .checkField("status", "getStatus", IdentifierStatus.UNKNOWN)
            .checkFieldIsNull("errorExplanation", "getErrorExplanation")
            .checkAll()
    }

    @Test
    fun parcelizationThereAndBackAgainFilled() {
        val features = FeaturesInternal(true, IdentifierStatus.UNKNOWN, "some error")
        val parcel = Parcel.obtain()
        features.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val result = FeaturesInternal(parcel)
        assertThat(result).isEqualToComparingFieldByField(features)
    }

    @Test
    fun parcelizationThereAndBackAgainEmpty() {
        val features = FeaturesInternal()
        val parcel = Parcel.obtain()
        features.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val result = FeaturesInternal(parcel)
        assertThat(result).isEqualToComparingFieldByField(features)
    }
}
