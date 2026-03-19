package io.appmetrica.analytics.impl.component.session

import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class SessionFromPastFactory(
    private val componentUnit: ComponentUnit,
    private val sessionIDProvider: SessionIDProvider,
) : ISessionFactory<SessionArguments> {

    private val tag = "[SessionFromPastFactory]"

    private val sessionStorage = SessionStorageImpl(
        componentUnit.componentPreferences,
        BackgroundSessionFactory.SESSION_TAG
    )

    private val sessionFactoryArguments: SessionFactoryArguments =
        SessionFactoryArguments.newBuilder(SessionType.BACKGROUND)
            .withSessionTimeout(BackgroundSessionFactory.SESSION_TIMEOUT_SEC)
            .build()

    // Session will be created as background session and will be loaded via BackgroundSessionFactory
    override fun load(): Session? = null

    override fun create(arguments: SessionArguments): Session {
        DebugLogger.info(
            tag,
            "%s create background session stub with requestParams: %s",
            componentUnit.componentId,
            arguments.sessionRequestParams
        )

        val sessionId = sessionIDProvider.getNextSessionId()

        sessionStorage.putSessionId(sessionId)
            .putSleepStart(arguments.creationElapsedRealtime)
            .putCreationTime(arguments.creationElapsedRealtime)
            .putCreationCurrentTimeMillis(arguments.creationTimestamp)
            .putReportId(SessionDefaults.INITIAL_REPORT_ID)
            .putAliveReportNeeded(false)
            .apply()

        componentUnit.dbHelper.newSessionFromPast(
            sessionId,
            SessionType.BACKGROUND,
            TimeUnit.MILLISECONDS.toSeconds(arguments.creationTimestamp),
            arguments.sessionRequestParams
        )

        val internalArguments = SessionArgumentsInternal.newBuilder(sessionFactoryArguments)
            .withAliveNeeded(sessionStorage.isAliveReportNeeded)
            .withCurrentReportId(sessionStorage.reportId)
            .withCreationTime(sessionStorage.creationTime)
            .withCreationCurrentTimeMillis(sessionStorage.creationCurrentTimeMillis)
            .withId(sessionStorage.sessionId)
            .withSleepStart(sessionStorage.sleepStart)
            .withLastEventOffset(sessionStorage.lastEventOffset)
            .build()

        return Session(componentUnit, sessionStorage, internalArguments)
    }
}
