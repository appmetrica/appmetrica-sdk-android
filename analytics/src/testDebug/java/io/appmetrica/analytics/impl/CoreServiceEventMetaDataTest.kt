package io.appmetrica.analytics.impl

import android.annotation.SuppressLint
import android.os.Bundle
import io.appmetrica.analytics.coreapi.internal.backport.Function
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.assertions.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@SuppressLint("RobolectricUsage") // CoreServiceEvent factories use Bundle / permissions state
@RunWith(ParameterizedRobolectricTestRunner::class)
internal class CoreServiceEventMetaDataTest(
    private val reportProvider: Function<CoreServiceEvent, CoreServiceEvent>,
    expectedType: InternalEvents,
    private val expectedName: String,
) : CommonTest() {

    @Rule
    @JvmField
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val expectedTypeId = expectedType.typeId

    @Test
    fun reportMetadata() {
        val originalValue = "original value"
        val originalProfileId = "original profile ID"
        val originalEventEnvironment = "original event environment"
        val originalElapsedRealtime = 7090L
        val originalCreationTimestamp = 666777L
        val originalPayload = Bundle().apply { putInt("some key", 100) }
        val extras = mapOf("key" to byteArrayOf(1, 3, 5, 7))
        val valueProtocolVersion = 2
        val serviceEvent = CoreServiceEvent().apply {
            type = InternalEvents.EVENT_TYPE_REGULAR.typeId
            customType = InternalEvents.EVENT_TYPE_APP_OPEN.typeId
            name = "original event"
            value = originalValue
            eventEnvironment = originalEventEnvironment
            profileID = originalProfileId
            bytesTruncated = 4
            creationElapsedRealtime = originalElapsedRealtime
            creationTimestamp = originalCreationTimestamp
            source = EventSource.JS
            payload = originalPayload
            this.extras = extras.toMutableMap()
            this.valueProtocolVersion = valueProtocolVersion
        }
        val resultServiceEvent = reportProvider.apply(serviceEvent)
        Assertions.ObjectPropertyAssertions(resultServiceEvent)
            .withIgnoredFields("systemTimeProvider", "value", "valueBytes", "valueBytesStorage", "isUndefinedType")
            .withPrivateFields(true)
            .withFinalFieldOnly(false)
            .checkField("name", expectedName)
            .checkField("eventEnvironment", originalEventEnvironment)
            .checkField("type", expectedTypeId)
            .checkField("customType", 0)
            .checkField("bytesTruncated", 0)
            .checkField("profileID", originalProfileId)
            .checkField("creationElapsedRealtime", originalElapsedRealtime)
            .checkField("creationTimestamp", originalCreationTimestamp)
            .checkField("firstOccurrenceStatus", FirstOccurrenceStatus.UNKNOWN)
            .checkFieldIsNull("source")
            .checkFieldIsNull("attributionIdChanged")
            .checkFieldIsNull("openId")
            .checkField("payload", originalPayload)
            .checkField("extras", extras)
            .checkField("valueProtocolVersion", valueProtocolVersion)
            .checkAll()
        if (resultServiceEvent.value != null) {
            assertThat(resultServiceEvent.value).isNotEqualTo(originalValue)
        }
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> { CoreServiceEvent.formAliveReportData(it) },
                InternalEvents.EVENT_TYPE_ALIVE,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> {
                    CoreServiceEvent.formFeaturesReportData(it, "some value")
                },
                InternalEvents.EVENT_TYPE_APP_FEATURES,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> { CoreServiceEvent.formFirstEventReportData(it) },
                InternalEvents.EVENT_TYPE_FIRST_ACTIVATION,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> { CoreServiceEvent.formInitReportData(it) },
                InternalEvents.EVENT_TYPE_INIT,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> {
                    CoreServiceEvent.formPermissionsReportData(
                        it,
                        ArrayList(),
                        null,
                        null,
                        ArrayList()
                    )
                },
                InternalEvents.EVENT_TYPE_PERMISSIONS,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> {
                    CoreServiceEvent.formSessionStartReportData(it, null)
                },
                InternalEvents.EVENT_TYPE_START,
                ""
            ),
            arrayOf(
                Function<CoreServiceEvent, CoreServiceEvent> { CoreServiceEvent.formUpdateReportData(it) },
                InternalEvents.EVENT_TYPE_APP_UPDATE,
                ""
            ),
        )
    }
}
