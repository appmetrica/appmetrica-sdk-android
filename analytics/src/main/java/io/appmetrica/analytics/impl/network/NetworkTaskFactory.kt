package io.appmetrica.analytics.impl.network

import io.appmetrica.analytics.coreutils.internal.executors.BlockingExecutor
import io.appmetrica.analytics.coreutils.internal.executors.SynchronizedBlockingExecutor
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.LazyReportConfigProvider
import io.appmetrica.analytics.impl.ReportTask
import io.appmetrica.analytics.impl.StartupTask
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.request.Obfuscator
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender
import io.appmetrica.analytics.impl.request.appenders.StartupParamsAppender
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.networktasks.internal.AESRSARequestBodyEncrypter
import io.appmetrica.analytics.networktasks.internal.AllHostsExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.DefaultResponseValidityChecker
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffDataHolder
import io.appmetrica.analytics.networktasks.internal.FinalConfigProvider
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder

internal object NetworkTaskFactory {

    private val backOffHolders = mutableMapOf<NetworkHost, ExponentialBackoffDataHolder>()
    private val userAgent = UserAgentProvider().userAgent

    @Synchronized
    private fun getBackoffHolder(host: NetworkHost): ExponentialBackoffDataHolder {
        return backOffHolders.getOrPut(host) {
            ExponentialBackoffDataHolder(
                HostRetryInfoProviderImpl(
                    GlobalServiceLocator.getInstance().servicePreferences,
                    host
                ),
                host.name
            )
        }
    }

    @JvmStatic
    fun createReportTask(component: ComponentUnit): NetworkTask {
        val requestBodyEncrypter = AESRSARequestBodyEncrypter()
        val paramsAppender = ReportParamsAppender(requestBodyEncrypter)
        val requestConfigProvider = LazyReportConfigProvider(component)
        return NetworkTask(
            BlockingExecutor(),
            ConnectionBasedExecutionPolicy(
                component.context
            ),
            AllHostsExponentialBackoffPolicy(getBackoffHolder(NetworkHost.REPORT)),
            ReportTask(
                component,
                paramsAppender,
                requestConfigProvider,
                FullUrlFormer(paramsAppender, requestConfigProvider),
                RequestDataHolder(),
                ResponseDataHolder(
                    DefaultResponseValidityChecker()
                ),
                requestBodyEncrypter
            ),
            listOf(Utils.notIsBadRequestCondition()),
            userAgent
        )
    }

    @JvmStatic
    fun createStartupTask(startupUnit: StartupUnit, requestConfig: StartupRequestConfig): NetworkTask {
        val paramsAppender = StartupParamsAppender(Obfuscator(), GlobalServiceLocator.getInstance().modulesController)
        val configProvider = FinalConfigProvider(requestConfig)
        return NetworkTask(
            SynchronizedBlockingExecutor(),
            ConnectionBasedExecutionPolicy(
                startupUnit.context
            ),
            AllHostsExponentialBackoffPolicy(getBackoffHolder(NetworkHost.STARTUP)),
            StartupTask(
                startupUnit,
                FullUrlFormer(
                    paramsAppender,
                    configProvider
                ),
                RequestDataHolder(),
                ResponseDataHolder(
                    DefaultResponseValidityChecker()
                ),
                configProvider
            ),
            emptyList(),
            userAgent
        )
    }
}
