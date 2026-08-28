package io.appmetrica.analytics.impl.db.event

import android.content.Context
import io.appmetrica.analytics.impl.AppEnvironment
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.PhoneUtils
import io.appmetrica.analytics.impl.ServiceEvent
import io.appmetrica.analytics.impl.component.session.SessionState
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.db.state.converter.EventExtrasConverter
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.MobileConnectionDescription
import io.appmetrica.analytics.impl.telephony.TelephonyInfoAdapter

internal class DbEventModelFactory @JvmOverloads constructor(
    private val context: Context,
    private val sessionState: SessionState,
    private val reportType: Int,
    private val vitalComponentDataProvider: VitalComponentDataProvider,
    private val serviceEvent: ServiceEvent,
    private val reportRequestConfig: ReportRequestConfig,
    private val environmentRevision: AppEnvironment.EnvironmentRevision,
    private val eventExtrasConverter: EventExtrasConverter = EventExtrasConverter(),
    private val dbLocationModelFactory: DbLocationModelFactory = DbLocationModelFactory(reportRequestConfig)
) {
    fun create() = DbEventModel(
        session = sessionState.sessionId,
        sessionType = sessionState.sessionType,
        numberInSession = sessionState.reportId,
        type = InternalEvents.valueOf(serviceEvent.type),
        globalNumber = if (EventsManager.shouldGenerateGlobalNumber(reportType)) {
            vitalComponentDataProvider.getAndIncrementEventGlobalNumber()
        } else { 0 },
        time = sessionState.reportTime,
        description = getEventDescription()
    )

    private fun getEventDescription(): DbEventModel.Description {
        return DbEventModel.Description(
            customType = serviceEvent.customType,
            name = serviceEvent.name,
            value = serviceEvent.value,
            numberOfType = vitalComponentDataProvider.getAndIncrementNumberOfType(reportType),
            locationInfo = dbLocationModelFactory.create(),
            errorEnvironment = serviceEvent.eventEnvironment,
            appEnvironment = environmentRevision.value,
            appEnvironmentRevision = environmentRevision.revisionNumber,
            truncated = serviceEvent.bytesTruncated,
            connectionType = PhoneUtils.getConnectionTypeInServerFormat(context),
            cellularConnectionType = getMobileConnectionDescription(),
            profileId = serviceEvent.profileID,
            firstOccurrenceStatus = serviceEvent.firstOccurrenceStatus,
            source = serviceEvent.source,
            attributionIdChanged = serviceEvent.attributionIdChanged,
            openId = serviceEvent.openId,
            extras = eventExtrasConverter.fromModel(serviceEvent.extras),
            valueProtocolVersion = serviceEvent.valueProtocolVersion
        )
    }

    private fun getMobileConnectionDescription(): String? {
        var result: String? = null
        GlobalServiceLocator.getInstance().telephonyDataProvider
            .adoptMobileConnectionDescription(object : TelephonyInfoAdapter<MobileConnectionDescription?> {
                override fun adopt(value: MobileConnectionDescription?) {
                    result = value?.networkType
                }
            })
        return result
    }
}
