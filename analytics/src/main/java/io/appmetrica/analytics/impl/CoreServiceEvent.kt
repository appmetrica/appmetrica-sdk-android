package io.appmetrica.analytics.impl

import android.os.Bundle
import android.util.Base64
import io.appmetrica.analytics.coreapi.internal.event.CounterReportApi
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.limitation.StringByBytesTrimmer
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_ALIVE
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_FEATURES
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_APP_UPDATE
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_FIRST_ACTIVATION
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_INIT
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_PERMISSIONS
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_SEND_USER_PROFILE
import io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_START
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

internal class CoreServiceEvent : CounterReportApi {

    override var name: String? = StringUtils.EMPTY
    override var value: String? = null
    var eventEnvironment: String? = null
    override var type: Int = 0
    override var customType: Int = 0
    override var bytesTruncated: Int = 0
    var profileID: String? = null
    var creationElapsedRealtime: Long = 0
    var creationTimestamp: Long = 0
    var firstOccurrenceStatus: FirstOccurrenceStatus = FirstOccurrenceStatus.UNKNOWN
    var source: EventSource? = null
    var payload: Bundle? = null
    var attributionIdChanged: Boolean? = null
    var openId: Int? = null
    override var extras: MutableMap<String, ByteArray> = HashMap()
    override var valueProtocolVersion: Int? = null

    private val systemTimeProvider = SystemTimeProvider()

    constructor() {
        creationElapsedRealtime = systemTimeProvider.elapsedRealtime()
        creationTimestamp = systemTimeProvider.currentTimeMillis()
    }

    override var valueBytes: ByteArray?
        get() = value?.let { Base64.decode(it, Base64.DEFAULT) }
        set(bytes) {
            value = bytes?.let { String(Base64.encode(it, Base64.DEFAULT)) }
        }

    val isUndefinedType: Boolean
        get() = InternalEvents.EVENT_TYPE_UNDEFINED.typeId == type

    override fun toString(): String {
        return String.format(
            Locale.US,
            "[event: %s, type: %s, value: %s]",
            name,
            InternalEvents.valueOf(type).info,
            Utils.trimToSize(value, Limits.EVENT_VALUE_FOR_LOGS_LIMIT)
        )
    }

    companion object {

        private const val TAG = "[CoreServiceEvent]"
        private const val PAYLOAD_CRASH_ID = "payload_crash_id"

        @JvmStatic
        fun from(counterReport: CounterReport): CoreServiceEvent {
            return CoreServiceEvent().apply {
                name = counterReport.name
                value = counterReport.value
                eventEnvironment = counterReport.eventEnvironment
                type = counterReport.type
                customType = counterReport.customType
                bytesTruncated = counterReport.bytesTruncated
                profileID = counterReport.profileID
                creationElapsedRealtime = counterReport.creationElapsedRealtime
                creationTimestamp = counterReport.creationTimestamp
                source = counterReport.source
                payload = counterReport.payload
                extras = HashMap(counterReport.extras)
                valueProtocolVersion = counterReport.valueProtocolVersion
            }
        }

        @JvmStatic
        fun formReportCopyingMetadata(serviceEvent: CoreServiceEvent): CoreServiceEvent {
            return CoreServiceEvent().apply {
                creationTimestamp = serviceEvent.creationTimestamp
                creationElapsedRealtime = serviceEvent.creationElapsedRealtime
                eventEnvironment = serviceEvent.eventEnvironment
                payload = serviceEvent.payload
                extras = serviceEvent.extras
                valueProtocolVersion = serviceEvent.valueProtocolVersion
                profileID = serviceEvent.profileID
            }
        }

        @JvmStatic
        private fun formReportCopyingMetaDataWithType(
            serviceEvent: CoreServiceEvent,
            event: InternalEvents
        ): CoreServiceEvent {
            return formReportCopyingMetadata(serviceEvent).apply {
                type = event.typeId
            }
        }

        @JvmStatic
        fun formAliveReportData(serviceEvent: CoreServiceEvent): CoreServiceEvent {
            return formReportCopyingMetaDataWithType(serviceEvent, EVENT_TYPE_ALIVE)
        }

        @JvmStatic
        fun formSessionStartReportData(
            serviceEvent: CoreServiceEvent,
            buildId: String?
        ): CoreServiceEvent {
            val startReport = formReportCopyingMetaDataWithType(serviceEvent, EVENT_TYPE_START)
            val eventStart = EventStart(buildId)
            startReport.valueBytes = MessageNano.toByteArray(EventStartConverter().fromModel(eventStart))
            startReport.creationTimestamp = serviceEvent.creationTimestamp
            startReport.creationElapsedRealtime = serviceEvent.creationElapsedRealtime
            return startReport
        }

        @JvmStatic
        fun formInitReportData(serviceEvent: CoreServiceEvent): CoreServiceEvent {
            return formReportCopyingMetaDataWithType(serviceEvent, EVENT_TYPE_INIT)
        }

        @JvmStatic
        fun formPermissionsReportData(
            serviceEvent: CoreServiceEvent,
            newPermissions: Collection<PermissionState>,
            bgRestrictionsState: BackgroundRestrictionsState?,
            appStandbyBucket: String?,
            availableProviders: List<String>
        ): CoreServiceEvent {
            val resultData = formReportCopyingMetadata(serviceEvent)
            var value = StringUtils.EMPTY
            try {
                val permissions = JSONArray()
                for (state in newPermissions) {
                    permissions.put(JSONObject().put("name", state.name).put("granted", state.granted))
                }
                val backgroundRestrictions = JSONObject()
                if (bgRestrictionsState != null) {
                    backgroundRestrictions.put("background_restricted", bgRestrictionsState.mBackgroundRestricted)
                    backgroundRestrictions.put("app_standby_bucket", appStandbyBucket)
                }
                value = JSONObject()
                    .put("permissions", permissions)
                    .put("background_restrictions", backgroundRestrictions)
                    .put("available_providers", JSONArray(availableProviders))
                    .toString()
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, "error while forming permissions value")
            }
            resultData.type = EVENT_TYPE_PERMISSIONS.typeId
            resultData.value = value
            return resultData
        }

        @JvmStatic
        fun formFeaturesReportData(serviceEvent: CoreServiceEvent, value: String?): CoreServiceEvent {
            return formReportCopyingMetadata(serviceEvent).apply {
                type = EVENT_TYPE_APP_FEATURES.typeId
                this.value = value
            }
        }

        @JvmStatic
        fun formFirstEventReportData(serviceEvent: CoreServiceEvent): CoreServiceEvent {
            return formReportCopyingMetaDataWithType(serviceEvent, EVENT_TYPE_FIRST_ACTIVATION)
        }

        @JvmStatic
        fun formUpdateReportData(serviceEvent: CoreServiceEvent): CoreServiceEvent {
            return formReportCopyingMetaDataWithType(serviceEvent, EVENT_TYPE_APP_UPDATE)
        }

        @JvmStatic
        fun formUserProfileEvent(): CoreServiceEvent {
            return CoreServiceEvent().apply {
                type = EVENT_TYPE_SEND_USER_PROFILE.typeId
            }
        }

        @JvmStatic
        fun nativeCrashEntry(
            eventType: InternalEvents,
            nativeCrash: String,
            uuid: String,
            logger: PublicLogger,
            creationTimestamp: Long,
            eventEnvironment: String?
        ): CoreServiceEvent {
            val serviceEvent = CoreServiceEvent()
            serviceEvent.type = eventType.typeId
            serviceEvent.creationTimestamp = creationTimestamp
            serviceEvent.value = StringByBytesTrimmer(
                EventLimitationProcessor.REPORT_EXTENDED_VALUE_MAX_SIZE,
                "event extended value",
                logger
            ).trim(nativeCrash)
            serviceEvent.payload = Bundle().apply {
                putString(PAYLOAD_CRASH_ID, uuid)
            }
            serviceEvent.eventEnvironment = eventEnvironment
            return serviceEvent
        }

        @JvmStatic
        fun unhandledExceptionFromFileReportEntry(
            type: InternalEvents,
            eventName: String?,
            value: ByteArray?,
            bytesTruncated: Int,
            errorEnvironment: String?,
            creationTimestamp: Long
        ): CoreServiceEvent {
            return CoreServiceEvent().apply {
                name = eventName
                valueBytes = value
                this.type = type.typeId
                this.bytesTruncated = bytesTruncated
                eventEnvironment = errorEnvironment
                this.creationTimestamp = creationTimestamp
            }
        }
    }
}
