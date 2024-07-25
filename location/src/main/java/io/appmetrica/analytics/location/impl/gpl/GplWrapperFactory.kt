package io.appmetrica.analytics.location.impl.gpl

import android.content.Context
import android.location.LocationListener
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.gpllibrary.internal.GplLibraryWrapper
import io.appmetrica.analytics.gpllibrary.internal.IGplLibraryWrapper
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class GplWrapperFactory {

    private val tag = "[GplWrapperFactory]"

    fun create(context: Context, locationListener: LocationListener, executor: IHandlerExecutor): IGplLibraryWrapper {
        if (ReflectionUtils.detectClassExists("com.google.android.gms.location.LocationRequest")) {
            try {
                return GplLibraryWrapper(
                    context,
                    locationListener,
                    executor.looper,
                    executor,
                    TimeUnit.SECONDS.toMillis(1)
                )
            } catch (ex: Throwable) {
                DebugLogger.error(tag, ex, "could not create GplLibraryWrapper")
            }
        } else {
            DebugLogger.info(tag, "Google Play Location Library does not exist")
        }
        return DummyGplLibraryWrapper()
    }
}
