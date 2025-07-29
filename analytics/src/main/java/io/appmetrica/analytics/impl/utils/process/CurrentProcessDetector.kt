package io.appmetrica.analytics.impl.utils.process

import android.os.Build
import io.appmetrica.analytics.coreutils.internal.AndroidUtils.isApiAchieved

class CurrentProcessDetector(
    private val processNameProvider: ProcessNameProvider = getProcessNameProvider()
) : ProcessNameProvider {

    /** @return The name of the current process. E.g. "org.chromium.chrome:privileged_process0".
     */
    override fun getProcessName(): String? {
        return processNameProvider.getProcessName()
    }

    fun isMainProcess(): Boolean {
        return try {
            val name = getProcessName() ?: return false
            name.isNotEmpty() && ":" !in name
        } catch (e: Throwable) {
            false
        }
    }

    fun isNonMainProcess(privateProcessName: String): Boolean {
        return try {
            val name = getProcessName() ?: return false
            name.isNotEmpty() && name.endsWith(":$privateProcessName")
        } catch (e: Throwable) {
            false
        }
    }

    companion object {
        fun getProcessNameProvider(): ProcessNameProvider {
            return if (isApiAchieved(Build.VERSION_CODES.P)) {
                ProcessNameProviderForP()
            } else {
                ProcessNameProviderBeforeP()
            }
        }
    }
}
