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

internal class CoreClientEvent(
    override val type: Int = 0,
    override val customType: Int = 0,
    override val name: String? = StringUtils.EMPTY,
    override val valueProtocolVersion: Int? = null,
    override val bytesTruncated: Int = 0,
    override val extras: Map<String, ByteArray> = emptyMap(),
    override val profileID: String? = null,
    override val eventEnvironment: String? = null,
    override val valueBytes: ByteArray? = StringUtils.getUTF8Bytes(StringUtils.EMPTY),
    val trimPolicy: EventTrimPolicy = EventTrimPolicy.STANDARD,
    val source: EventSource? = null,
    val payload: Bundle? = null,
) : ClientEvent {

    private val systemTimeProvider = SystemTimeProvider()

    val creationElapsedRealtime: Long = systemTimeProvider.elapsedRealtime()
    val creationTimestamp: Long = systemTimeProvider.currentTimeMillis()

    override fun toString(): String {
        return "[" +
            "event: $name, " +
            "type: ${InternalEvents.valueOf(type).info}, " +
            "value: ${valueBytes?.let {
                Utils.trimToSize(String(it, StandardCharsets.UTF_8), Limits.EVENT_VALUE_FOR_LOGS_LIMIT)
            }}" +
            "]"
    }

    companion object {

        private const val TAG = "[CoreClientEvent]"

        @JvmStatic
        fun formUpdatePreActivationConfig(): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_UPDATE_PRE_ACTIVATION_CONFIG.typeId,
                trimPolicy = EventTrimPolicy.NONE,
            )
        }

        @JvmStatic
        fun formJsInitEvent(value: String): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_WEBVIEW_SYNC.typeId,
                valueBytes = StringUtils.getUTF8Bytes(value),
                source = EventSource.JS,
                trimPolicy = EventTrimPolicy.NONE,
            )
        }

        @JvmStatic
        fun formAppEnvironmentChangedReport(key: String, value: String): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_UPDATED.typeId,
                valueBytes = StringUtils.getUTF8Bytes(JsonHelper.mapToJsonString(hashMapOf(key to value))),
                trimPolicy = EventTrimPolicy.NONE,
            )
        }

        @JvmStatic
        fun formAppEnvironmentClearedReport(): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_APP_ENVIRONMENT_CLEARED.typeId,
                trimPolicy = EventTrimPolicy.NONE,
            )
        }

        @JvmStatic
        fun formUserProfileEvent(userProfile: Userprofile.Profile): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SEND_USER_PROFILE.typeId,
                valueBytes = MessageNano.toByteArray(userProfile),
                trimPolicy = EventTrimPolicy.NONE,
            )
        }

        @JvmStatic
        fun formSetUserProfileIDEvent(
            userProfileID: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SET_USER_PROFILE_ID.typeId,
                profileID = userProfileID,
                valueBytes = userProfileID?.let { StringUtils.getUTF8Bytes(it) },
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun formRevenueEvent(
            revenue: RevenueWrapper,
        ): CoreClientEvent {
            val result = revenue.dataToSend
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SEND_REVENUE_EVENT.typeId,
                valueBytes = result.first,
                bytesTruncated = result.second ?: 0,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun formAdRevenueEvent(
            adRevenue: AdRevenueWrapper,
        ): CoreClientEvent {
            val result = adRevenue.getDataToSend()
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SEND_AD_REVENUE_EVENT.typeId,
                valueBytes = result.first,
                bytesTruncated = result.second,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun formECommerceEvent(
            result: Result<Ecommerce.ECommerceEvent, BytesTruncatedProvider>,
        ): CoreClientEvent {
            val valueBytesRaw = MessageNano.toByteArray(result.result)
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SEND_ECOMMERCE_EVENT.typeId,
                valueBytes = try {
                    GZIPUtils.gzipBytes(valueBytesRaw)
                } catch (_: Throwable) {
                    null
                },
                bytesTruncated = result.bytesTruncated,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun formJsEvent(
            eventName: String,
            eventValue: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = eventValue?.let { StringUtils.getUTF8Bytes(it) },
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId,
                source = EventSource.JS,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun reportEntry(
            eventType: InternalEvents,
        ): CoreClientEvent {
            return CoreClientEvent(
                type = eventType.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun reportEntry(
            eventType: InternalEvents,
            payload: Bundle?
        ): CoreClientEvent {
            return CoreClientEvent(
                type = eventType.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                payload = payload,
            )
        }

        @JvmStatic
        fun regularEventReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun regularEventReportEntry(
            eventName: String?,
            attributes: Map<String?, Any?>?
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = StringUtils.getUTF8Bytes(JsonHelper.mapToJsonString(attributes)),
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun regularEventReportEntry(
            eventName: String?,
            extraData: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = extraData?.let { StringUtils.getUTF8Bytes(it) },
                type = InternalEvents.EVENT_TYPE_REGULAR.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun anrEntry(
            value: ByteArray?,
            reporterEnvironment: ReporterEnvironment,
        ): CoreClientEvent {
            return CoreClientEvent(
                valueBytes = value,
                name = StringUtils.EMPTY,
                type = InternalEvents.EVENT_TYPE_ANR.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                eventEnvironment = reporterEnvironment.errorEnvironment,
            )
        }

        @JvmStatic
        fun regularErrorReportEntry(
            eventName: String?,
            extraData: ByteArray?,
            reporterEnvironment: ReporterEnvironment,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = extraData,
                type = InternalEvents.EVENT_TYPE_EXCEPTION_USER_PROTOBUF.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                eventEnvironment = reporterEnvironment.errorEnvironment,
            )
        }

        @JvmStatic
        fun customErrorReportEntry(
            eventName: String?,
            extraData: ByteArray,
            reporterEnvironment: ReporterEnvironment,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = extraData,
                type = InternalEvents.EVENT_TYPE_EXCEPTION_USER_CUSTOM_PROTOBUF.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                eventEnvironment = reporterEnvironment.errorEnvironment,
            )
        }

        @JvmStatic
        fun notifyServiceOnActivityStartReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                type = InternalEvents.EVENT_TYPE_START.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun activityEndReportEntry(
            eventName: String?,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                type = InternalEvents.EVENT_TYPE_UPDATE_FOREGROUND_TIME.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun unhandledExceptionReportEntry(
            eventName: String?,
            value: ByteArray?,
            reporterEnvironment: ReporterEnvironment,
        ): CoreClientEvent {
            return CoreClientEvent(
                name = eventName,
                valueBytes = value,
                type = InternalEvents.EVENT_TYPE_EXCEPTION_UNHANDLED_PROTOBUF.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                eventEnvironment = reporterEnvironment.errorEnvironment,
            )
        }

        @JvmStatic
        fun requestReferrerEntry(
            payload: Bundle?
        ): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_REQUEST_REFERRER.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
                payload = payload
            )
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
            return CoreClientEvent(
                valueBytes = StringUtils.getUTF8Bytes(JsonHelper.mapToJsonString(map)),
                name = StringUtils.EMPTY,
                type = InternalEvents.EVENT_TYPE_APP_OPEN.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun activationEventReportEntry(
            preloadInfo: PreloadInfoWrapper?,
            userProfileID: String?,
        ): CoreClientEvent {
            val activationEventValue = JSONObject()
            preloadInfo?.addToEventValue(activationEventValue)
            return CoreClientEvent(
                valueBytes = StringUtils.getUTF8Bytes(activationEventValue.toString()),
                name = StringUtils.EMPTY,
                type = InternalEvents.EVENT_TYPE_ACTIVATION.typeId,
                profileID = userProfileID,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun cleanupEventReportEntry(
            value: String,
        ): CoreClientEvent {
            return CoreClientEvent(
                valueBytes = StringUtils.getUTF8Bytes(value),
                name = StringUtils.EMPTY,
                type = InternalEvents.EVENT_TYPE_CLEANUP.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun customEventReportEntry(
            moduleEvent: ModuleEvent,
        ): CoreClientEvent {
            return CoreClientEvent(
                valueBytes = if (!Utils.isNullOrEmpty(moduleEvent.attributes)) {
                    StringUtils.getUTF8Bytes(JsonHelper.mapToJsonString(moduleEvent.attributes))
                } else {
                    moduleEvent.valueBytes
                },
                name = moduleEvent.name,
                type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId,
                customType = moduleEvent.type,
                valueProtocolVersion = 2,
                source = EventCategoryToSourceConverter().convert(moduleEvent.category),
                eventEnvironment = JsonHelper.mapToJsonString(moduleEvent.environment),
                extras = moduleEvent.extras?.let { HashMap(it) } ?: HashMap(),
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun setSessionExtraReportEntry(
            key: String,
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent(
                type = InternalEvents.EVENT_TYPE_SET_SESSION_EXTRA.typeId,
                extras = HashMap(Collections.singletonMap(key, value ?: ByteArray(0))),
                trimPolicy = EventTrimPolicy.STANDARD,
            )
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
            return CoreClientEvent(
                valueBytes = event.data,
                name = event.name,
                type = InternalEvents.EVENT_TYPE_CUSTOM_EVENT.typeId,
                customType = event.type,
                bytesTruncated = event.bytesTruncated,
                valueProtocolVersion = 2,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }

        @JvmStatic
        fun clientExternalAttributionEntry(
            value: ByteArray?,
        ): CoreClientEvent {
            return CoreClientEvent(
                valueBytes = value,
                name = StringUtils.EMPTY,
                type = InternalEvents.EVENT_CLIENT_EXTERNAL_ATTRIBUTION.typeId,
                trimPolicy = EventTrimPolicy.STANDARD,
            )
        }
    }
}
