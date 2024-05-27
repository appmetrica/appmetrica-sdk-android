package io.appmetrica.analytics.impl.component

import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.impl.component.session.BackgroundSessionFactory
import io.appmetrica.analytics.impl.component.session.ForegroundSessionFactory
import io.appmetrica.analytics.impl.component.session.SessionStorageImpl
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class ComponentMigrationToV113(
    componentUnit: ComponentUnit
) : ComponentMigrationScript(componentUnit, "[ComponentMigrationToV113]") {

    override fun shouldMigrate(from: Int): Boolean = from < SdkData.MIGRATE_SESSION_SLEEP_START_TIME_TO_MILLISECONDS

    override fun run() {
        DebugLogger.info(tag, "Migrate...")
        val preferences = componentUnit.componentPreferences
        try {
            val backgroundSessionStorage = SessionStorageImpl(preferences, BackgroundSessionFactory.SESSION_TAG)
            backgroundSessionStorage.sleepStart?.let {
                backgroundSessionStorage.putSleepStart(TimeUnit.SECONDS.toMillis(it))
            }
            backgroundSessionStorage.lastEventOffset?.let {
                backgroundSessionStorage.putLastEventOffset(TimeUnit.SECONDS.toMillis(it))
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        }
        try {
            val foregroundSessionStorage = SessionStorageImpl(preferences, ForegroundSessionFactory.SESSION_TAG)
            foregroundSessionStorage.sleepStart?.let {
                foregroundSessionStorage.putSleepStart(TimeUnit.SECONDS.toMillis(it))
            }
            foregroundSessionStorage.lastEventOffset?.let {
                foregroundSessionStorage.putLastEventOffset(TimeUnit.SECONDS.toMillis(it))
            }
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        }
        DebugLogger.info(tag, "Migration finished.")
    }
}
