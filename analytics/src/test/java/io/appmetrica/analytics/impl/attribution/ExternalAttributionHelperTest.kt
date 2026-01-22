package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.startup.ExternalAttributionConfig
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ExternalAttributionHelperTest : CommonTest() {

    private val collectingInterval = 100500L
    private val externalAttributionConfig = ExternalAttributionConfig(
        collectingInterval
    )
    private val startupState: StartupState = mock {
        on { externalAttributionConfig } doReturn externalAttributionConfig
    }

    private val externalAttributionWindowStart = 100L
    private val vitalComponentDataProvider: VitalComponentDataProvider = mock {
        on { externalAttributionWindowStart } doReturn externalAttributionWindowStart
    }

    private val componentPreferences: PreferencesComponentDbStorage = mock()

    private val componentUnit: ComponentUnit = mock {
        on { componentPreferences } doReturn componentPreferences
        on { startupState } doReturn startupState
        on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
    }
    private val timeProvider: TimeProvider = mock()

    private val helper = ExternalAttributionHelper(componentUnit, timeProvider)

    @Test
    fun isInAttributionCollectingWindow() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(externalAttributionWindowStart + collectingInterval - 1)
        assertThat(helper.isInAttributionCollectingWindow()).isTrue()
        verify(vitalComponentDataProvider, never()).externalAttributionWindowStart = any()
    }

    @Test
    fun isInAttributionCollectingWindowIfNoCollectingInterval() {
        whenever(startupState.externalAttributionConfig).thenReturn(null)
        assertThat(helper.isInAttributionCollectingWindow()).isFalse()
    }

    @Test
    fun isInAttributionCollectingWindowIfOutOfCollectingInterval() {
        whenever(timeProvider.currentTimeMillis())
            .thenReturn(externalAttributionWindowStart + collectingInterval + 1)
        assertThat(helper.isInAttributionCollectingWindow()).isFalse()
        verify(vitalComponentDataProvider, never()).externalAttributionWindowStart = any()
    }

    @Test
    fun isInAttributionCollectingWindowIfNoExternalAttributionWindowStart() {
        whenever(vitalComponentDataProvider.externalAttributionWindowStart).thenReturn(-1)
        val currentTime = 10431L
        whenever(timeProvider.currentTimeMillis()).thenReturn(currentTime)

        helper.isInAttributionCollectingWindow()

        verify(vitalComponentDataProvider).externalAttributionWindowStart = currentTime
    }

    @Test
    fun isNewAttributionIfTheSameIsInStorage() {
        val type = 12
        val data = JSONObject().apply {
            put("key", "value")
        }.toString()
        whenever(componentPreferences.sentExternalAttributions).thenReturn(
            mapOf(
                type to data
            )
        )

        assertThat(helper.isNewAttribution(type, data)).isFalse()
    }

    @Test
    fun isNewAttributionIfAnotherIsInStorage() {
        val type = 12
        val firstData = JSONObject().apply {
            put("key1", "value1")
        }.toString()
        val secondData = JSONObject().apply {
            put("key2", "value2")
        }.toString()
        whenever(componentPreferences.sentExternalAttributions).thenReturn(
            mapOf(
                type to firstData
            )
        )

        assertThat(helper.isNewAttribution(type, secondData)).isTrue()
    }

    @Test
    fun isNewAttributionIfNoAttributionOfGivenTypeInStorage() {
        val type = 12
        val data = JSONObject().apply {
            put("key", "value")
        }.toString()
        whenever(componentPreferences.sentExternalAttributions).thenReturn(mapOf())

        assertThat(helper.isNewAttribution(type, data)).isTrue()
    }

    @Test
    fun isNewAttributionIfInvalidAttributionOfGivenTypeInStorage() {
        val type = 12
        val data = JSONObject().apply {
            put("key", "value")
        }.toString()
        whenever(componentPreferences.sentExternalAttributions).thenReturn(
            mapOf(
                type to "someString"
            )
        )

        assertThat(helper.isNewAttribution(type, data)).isTrue()
    }

    @Test
    fun saveAttribution() {
        val firstType = 12
        val firstData = JSONObject().apply {
            put("key1", "value1")
        }.toString()
        val secondType = 21
        val secondData = JSONObject().apply {
            put("key2", "value2")
        }.toString()
        val thirdType = 12
        val thirdData = JSONObject().apply {
            put("key3", "value3")
        }.toString()
        whenever(componentPreferences.sentExternalAttributions).thenReturn(
            mapOf(
                firstType to firstData,
                secondType to secondData
            )
        )

        helper.saveAttribution(thirdType, thirdData)

        verify(componentPreferences).putSentExternalAttributions(
            mapOf(
                secondType to secondData,
                thirdType to thirdData
            )
        )
    }
}
