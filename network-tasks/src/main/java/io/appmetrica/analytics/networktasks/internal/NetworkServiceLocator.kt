package io.appmetrica.analytics.networktasks.internal

import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreapi.internal.io.IExecutionPolicy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class NetworkServiceLocator @AnyThread @VisibleForTesting constructor(
    executionPolicy: IExecutionPolicy,
) : NetworkServiceLifecycleObserver {

    private val tag = "[NetworkServiceLocator]"

    val networkCore: NetworkCore = NetworkCore(executionPolicy).apply {
        name = "IAA-NC"
        start()
        DebugLogger.info(tag, "network core started")
    }

    override fun onCreate() {
        // do nothing
    }

    override fun onDestroy() {
        DebugLogger.info(tag, "onDestroy")
        networkCore.stopTasks()
    }

    companion object {

        @Volatile
        private lateinit var instance: NetworkServiceLocator

        @JvmStatic
        fun getInstance(): NetworkServiceLocator {
            return instance
        }

        @JvmStatic
        @AnyThread
        fun init(executionPolicy: IExecutionPolicy) {
            if (!::instance.isInitialized) {
                synchronized(NetworkServiceLocator::class.java) {
                    if (!::instance.isInitialized) {
                        instance = NetworkServiceLocator(executionPolicy)
                    }
                }
            }
        }

        @JvmStatic
        @VisibleForTesting
        fun init(networkServiceLocator: NetworkServiceLocator) {
            instance = networkServiceLocator
        }
    }
}
