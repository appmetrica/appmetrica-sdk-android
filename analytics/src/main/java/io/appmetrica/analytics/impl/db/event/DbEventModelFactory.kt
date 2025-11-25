package io.appmetrica.analytics.impl.db.event

import android.content.Context
import io.appmetrica.analytics.impl.AppEnvironment
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.PhoneUtils
import io.appmetrica.analytics.impl.component.session.SessionState
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.db.state.converter.EventExtrasConverter
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.MobileConnectionDescription
import io.appmetrica.analytics.impl.telephony.TelephonyInfoAdapter
import io.appmetrica.analytics.impl.utils.encryption.EncryptedCounterReport

internal class DbEventModelFactory @JvmOverloads constructor(
    private val context: Context,
    private val sessionState: SessionState,
    private val reportType: Int,
    private val vitalComponentDataProvider: VitalComponentDataProvider,
    private val encryptedCounterReport: EncryptedCounterReport,
    private val reportRequestConfig: ReportRequestConfig,
    private val environmentRevision: AppEnvironment.EnvironmentRevision,
    private val eventExtrasConverter: EventExtrasConverter = EventExtrasConverter(),
    private val dbLocationModelFactory: DbLocationModelFactory = DbLocationModelFactory(reportRequestConfig)
) {
    private val reportData = encryptedCounterReport.mCounterReport

    fun create() = DbEventModel(
        session = sessionState.sessionId,
        sessionType = sessionState.sessionType,
        numberInSession = sessionState.reportId,
        type = InternalEvents.valueOf(reportData.type),
        globalNumber = if (EventsManager.shouldGenerateGlobalNumber(reportType)) {
            vitalComponentDataProvider.getAndIncrementEventGlobalNumber()
        } else { 0 },
        time = sessionState.reportTime,
        description = getEventDescription()
    )

    private fun getEventDescription(): DbEventModel.Description {
        return DbEventModel.Description(
            customType = reportData.customType,
            name = reportData.name,
            value = reportData.value,
            numberOfType = vitalComponentDataProvider.getAndIncrementNumberOfType(reportType),
            locationInfo = dbLocationModelFactory.create(),
            errorEnvironment = reportData.eventEnvironment,
            appEnvironment = environmentRevision.value,
            appEnvironmentRevision = environmentRevision.revisionNumber,
            truncated = reportData.bytesTruncated,
            connectionType = PhoneUtils.getConnectionTypeInServerFormat(context),
            cellularConnectionType = getMobileConnectionDescription(),
            encryptingMode = encryptedCounterReport.mEventEncryptionMode,
            profileId = reportData.profileID,
            firstOccurrenceStatus = reportData.firstOccurrenceStatus,
            source = reportData.source,
            attributionIdChanged = reportData.attributionIdChanged,
            openId = reportData.openId,
            extras = eventExtrasConverter.fromModel(reportData.extras)
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
