package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils
import io.appmetrica.analytics.internal.CounterConfiguration

/**
 * Writes [EventIpcData] as flat keys into the IPC Bundle
 * (alongside CounterConfiguration / ProcessConfiguration).
 *
 * Do not remove event keys after decode — [ReportProxy] fans out the same Bundle.
 */
internal object EventIpcCodec {

    @JvmStatic
    fun toBundle(data: EventIpcData, bundle: Bundle): Bundle = bundle.apply {
        putString(EventIpcBundleKeys.EVENT, data.name)
        putString(EventIpcBundleKeys.VALUE, data.value)
        putInt(EventIpcBundleKeys.TYPE, data.type)
        putInt(EventIpcBundleKeys.CUSTOM_TYPE, data.customType)
        putInt(EventIpcBundleKeys.TRUNCATED, data.bytesTruncated)
        putString(EventIpcBundleKeys.PROFILE_ID, data.profileID)
        data.payload?.let { putBundle(EventIpcBundleKeys.PAYLOAD, it) }
        data.eventEnvironment?.let { putString(EventIpcBundleKeys.ENVIRONMENT, it) }
        putLong(EventIpcBundleKeys.CREATION_ELAPSED_REALTIME, data.creationElapsedRealtime)
        putLong(EventIpcBundleKeys.CREATION_TIMESTAMP, data.creationTimestamp)
        data.source?.let { putInt(EventIpcBundleKeys.SOURCE, it.code) }
        putBundle(EventIpcBundleKeys.EXTRAS, CollectionUtils.mapToBundle(data.extras))
        data.valueProtocolVersion?.let {
            putInt(EventIpcBundleKeys.VALUE_PROTOCOL_VERSION, it)
        }
    }

    @JvmStatic
    fun fromBundle(bundle: Bundle): EventIpcData {
        // payload may contain SDK Parcelables (IdentifiersData, ReferrerResultReceiver).
        // Callers usually set classLoader already; keep a defensive set for direct fromBundle use.
        bundle.classLoader = CounterConfiguration::class.java.classLoader
        return EventIpcData(
            name = bundle.getString(EventIpcBundleKeys.EVENT),
            value = bundle.getString(EventIpcBundleKeys.VALUE),
            // Bundle.getInt returns 0 when TYPE is absent, not EVENT_TYPE_UNDEFINED (-1).
            // 0 is EVENT_TYPE_INIT — that was the bug in the old CounterReport.fromBundle fallback.
            type = bundle.getInt(EventIpcBundleKeys.TYPE, -1),
            customType = bundle.getInt(EventIpcBundleKeys.CUSTOM_TYPE),
            bytesTruncated = bundle.getInt(EventIpcBundleKeys.TRUNCATED),
            profileID = bundle.getString(EventIpcBundleKeys.PROFILE_ID),
            eventEnvironment = bundle.getString(EventIpcBundleKeys.ENVIRONMENT),
            creationElapsedRealtime = bundle.getLong(EventIpcBundleKeys.CREATION_ELAPSED_REALTIME),
            creationTimestamp = bundle.getLong(EventIpcBundleKeys.CREATION_TIMESTAMP),
            source = if (bundle.containsKey(EventIpcBundleKeys.SOURCE)) {
                EventSource.fromCode(bundle.getInt(EventIpcBundleKeys.SOURCE))
            } else {
                null
            },
            payload = bundle.getBundle(EventIpcBundleKeys.PAYLOAD),
            extras = HashMap(
                CollectionUtils.bundleToMap(bundle.getBundle(EventIpcBundleKeys.EXTRAS))
            ),
            valueProtocolVersion = Utils.getIntOrNull(
                bundle,
                EventIpcBundleKeys.VALUE_PROTOCOL_VERSION
            ),
        )
    }
}
