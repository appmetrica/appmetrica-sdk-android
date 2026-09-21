package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.analytics.ModuleEvent
import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData
import io.appmetrica.analytics.coreapi.internal.event.ClientEvent
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.io.GZIPUtils
import io.appmetrica.analytics.coreutils.internal.limitation.BytesTruncatedProvider
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper
import io.appmetrica.analytics.impl.protobuf.backend.Ecommerce
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile
import io.appmetrica.analytics.impl.revenue.ad.AdRevenueWrapper
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Collections

internal class CoreClientEvent : ClientEvent {

    override var name: String? = StringUtils.EMPTY
    private var valueBytesStorage: ByteArray? = StringUtils.getUTF8Bytes(StringUtils.EMPTY)
    override var eventEnvironment: String? = null
    override var type: Int = 0
    override var customType: Int = 0
    override var bytesTruncated: Int = 0
    override var profileID: String? = null
    var creationElapsedRealtime: Long = 0
    var creationTimestamp: Long = 0
    var source: EventSource? = null
    var payload: Bundle? = null
    override var extras: MutableMap<String, ByteArray> = HashMap()
    override var valueProtocolVersion: Int? = null
    var trimPolicy: EventTrimPolicy = EventTrimPolicy.STANDARD

    private val systemTimeProvider = SystemTimeProvider()

    constructor() {
        creationElapsedRealtime = systemTimeProvider.elapsedRealtime()
        creationTimestamp = systemTimeProvider.currentTimeMillis()
    }

    override var value: String?
        get() = valueBytesStorage?.let { String(it, StandardCharsets.UTF_8) }
        set(stringValue) {
            valueBytesStorage = stringValue?.let { StringUtils.getUTF8Bytes(it) }
        }

    override var valueBytes: ByteArray?
        get() = valueBytesStorage
        set(bytes) {
            valueBytesStorage = bytes
        }

    override fun toString(): String {
        return "[" +
            "event: $name, " +
            "type: ${InternalEvents.valueOf(type).info}, " +
            "value: ${Utils.trimToSize(value, Limits.EVENT_VALUE_FOR_LOGS_LIMIT)}" +
            "]"
    }

    companion object {

        private const val TAG = "[CoreClientEvent]"

        @JvmStatic
        fun formUpdatePreActivationConfig(): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.typeId
                trimPolicy = EventTrimPolicy.NONE
            }
        }

        @JvmStatic
        fun formJsInitEvent(value: String): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.typeId
                this.value = value
                source = EventSource.JS
                trimPolicy = EventTrimPolicy.NONE
            }
        }

        @JvmStatic
        fun formAppEnvironmentChangedReport(key: String, value: String): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED.typeId
                this.value = JsonHelper.mapToJsonString(hashMapOf(key to value))
                trimPolicy = EventTrimPolicy.NONE
            }
        }

        @JvmStatic
        fun formAppEnvironmentClearedReport(): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED.typeId
                trimPolicy = EventTrimPolicy.NONE
            }
        }

        @JvmStatic
        fun formUserProfileEvent(): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.typeId
                trimPolicy = EventTrimPolicy.NONE
            }
        }

        @JvmStatic
        fun formUserProfileEvent(userProfile: Userprofile.Profile): CoreClientEvent {
            return formUserProfileEvent().apply {
                valueBytes = MessageNano.toByteArray(userProfile)
            }
        }

        @JvmStatic
        fun formSetUserProfileIDEvent(
            userProfileID: String?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId
                profileID = userProfileID
                value = userProfileID
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun formRevenueEvent(
            revenue: RevenueWrapper,
        ): CoreClientEvent {
            val result = revenue.dataToSend
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId
                valueBytes = result.first
                bytesTruncated = result.second ?: 0
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun formAdRevenueEvent(
            adRevenue: AdRevenueWrapper,
        ): CoreClientEvent {
            val result = adRevenue.getDataToSend()
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId
                valueBytes = result.first
                bytesTruncated = result.second
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun formECommerceEvent(
            result: Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>,
        ): CoreClientEvent {
            val valueBytesRaw = MessageNano.toByteArray(result.result)
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId
                valueBytes = try {
                    GZIPUtils.gzipBytes(valueBytesRaw)
                } catch (_: Throwable) {
                    null
                }
                bytesTruncated = result.bytesTruncated
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun formJsEvent(
            eventName: String,
            eventValue: String?,
        ): CoreClientEvent {
            return regularEventReportEntry(eventName, eventValue).apply {
                source = EventSource.JS
            }
        }

        @JvmStatic
        fun reportEntry(
            eventType: InternalEvents,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                type = eventType.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun regularEventReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun regularEventReportEntry(
            eventName: String?,
            extraData: String?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                value = extraData
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun anrEntry(
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                this.valueBytes = value
                name = StringUtils.EMPTY
                type = InternalEvents.EVENT_TYPE_ANR.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun regularErrorReportEntry(
            eventName: String?,
            extraData: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                valueBytes = extraData
                type = InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun customErrorReportEntry(
            eventName: String?,
            extraData: ByteArray,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                valueBytes = extraData
                type = InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun notifyServiceOnActivityStartReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                type = InternalEvents.EVENT_TYPE_START.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun activityEndReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                type = InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun unhandledExceptionReportEntry(
            eventName: String?,
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                name = eventName
                valueBytes = value
                type = InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun requestReferrerEntry(): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_REQUEST_REFERRER.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun openAppReportEntry(
            value: String?,
            auto: Boolean,
        ): CoreClientEvent {
            return eventOpenEntry(EventsManager.EVENT_OPEN_TYPE_OPEN, value, auto)
        }

        @JvmStatic
        fun eventOpenEntry(
            type: String,
            value: String?,
            auto: Boolean,
        ): CoreClientEvent {
            val map = hashMapOf<String, Any?>(
                EventsManager.EVENT_OPEN_TYPE_KEY to type,
                EventsManager.EVENT_OPEN_LINK_KEY to value,
                EventsManager.EVENT_OPEN_AUTO_KEY to auto,
            )
            return CoreClientEvent().apply {
                this.value = JsonHelper.mapToJsonString(map)
                name = StringUtils.EMPTY
                this.type = InternalEvents.EVENT_TYPE_APP_OPEN.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun activationEventReportEntry(
            preloadInfo: PreloadInfoWrapper?,
            userProfileID: String?,
        ): CoreClientEvent {
            val activationEventValue = JSONObject()
            preloadInfo?.addToEventValue(activationEventValue)
            return CoreClientEvent().apply {
                value = activationEventValue.toString()
                name = StringUtils.EMPTY
                type = InternalEvents.EVENT_TYPE_ACTIVATION.typeId
                profileID = userProfileID
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun cleanupEventReportEntry(
            value: String,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                this.value = value
                name = StringUtils.EMPTY
                type = InternalEvents.EVENT_TYPE_CLEANUP.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun customEventReportEntry(
            moduleEvent: ModuleEvent,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                valueBytes = moduleEvent.valueBytes
                name = moduleEvent.name
                type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId
                customType = moduleEvent.type
                valueProtocolVersion = 2
                source = EventCategoryToSourceConverter().convert(moduleEvent.category)
                eventEnvironment = JsonHelper.mapToJsonString(moduleEvent.environment)
                moduleEvent.extras?.let { extras = HashMap(it) }
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun setSessionExtraReportEntry(
            key: String,
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                type = InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.typeId
                extras = HashMap(Collections.singletonMap(key, value ?: ByteArray(0)))
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun appMetricaEventReportEntry(
            event: AppMetricaEventData,
        ): CoreClientEvent {
            DebugLogger.info(
                TAG,
                "Sending AppMetricaEvent: total size = %d, bytes truncated = %d",
                event.data.size,
                event.bytesTruncated
            )
            return CoreClientEvent().apply {
                valueBytes = event.data
                name = event.name
                type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId
                customType = event.type
                bytesTruncated = event.bytesTruncated
                valueProtocolVersion = 2
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }

        @JvmStatic
        fun clientExternalAttributionEntry(
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent().apply {
                valueBytes = value
                name = StringUtils.EMPTY
                type = InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId
                trimPolicy = EventTrimPolicy.STANDARD
            }
        }
    }
}
