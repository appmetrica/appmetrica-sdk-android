package io.appmetrica.analytics.impl.location.network

import android.content.Context
import io.appmetrica.analytics.BuildConfig
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.executors.BlockingExecutor
import io.appmetrica.analytics.coreutils.internal.network.UserAgent
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.LazyReportConfigProvider
import io.appmetrica.analytics.impl.ReportTask
import io.appmetrica.analytics.impl.StartupTask
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.network.ConnectionBasedExecutionPolicy
import io.appmetrica.analytics.impl.network.Constants
import io.appmetrica.analytics.impl.network.HostRetryInfoProviderImpl
import io.appmetrica.analytics.impl.network.NetworkHost
import io.appmetrica.analytics.impl.network.NetworkTaskFactory
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.request.appenders.ReportParamsAppender
import io.appmetrica.analytics.impl.request.appenders.StartupParamsAppender
import io.appmetrica.analytics.impl.startup.StartupUnit
import io.appmetrica.analytics.networktasks.internal.AESRSARequestBodyEncrypter
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffDataHolder
import io.appmetrica.analytics.networktasks.internal.ExponentialBackoffPolicy
import io.appmetrica.analytics.networktasks.internal.FinalConfigProvider
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer
import io.appmetrica.analytics.networktasks.internal.NetworkTask
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ConstructionArgumentCaptor
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class NetworkTaskFactoryTest : CommonTest() {

    private val customUserAgent = UserAgent.getFor(
        Constants.Config.LIBRARY_ID,
        BuildConfig.VERSION_NAME,
        BuildConfig.BUILD_NUMBER
    )
    val context = mock<Context>()
    val counterReport = mock<CounterReport>()
    val vitalComponentDataProvider = mock<VitalComponentDataProvider>()
    val notIsBadRequestCondition = mock<NetworkTask.ShouldTryNextHostCondition>()

    val componentUnit = mock<ComponentUnit> {
        on { context } doReturn context
    }

    val startupUnit = mock<StartupUnit> {
        on { context } doReturn context
    }
    val startupRequestConfig = mock<StartupRequestConfig>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val hostRetryProviderImplMockedRule = MockedConstructionRule(HostRetryInfoProviderImpl::class.java)

    @get:Rule
    val exponentialBackoffDataHolderMockedRule = MockedConstructionRule(ExponentialBackoffDataHolder::class.java)

    @get:Rule
    val fullUrlFormerMockedRule = MockedConstructionRule(FullUrlFormer::class.java)

    @get:Rule
    val utilsMockedStaticRule = MockedStaticRule(Utils::class.java)

    @get:Rule
    val reportParamsAppenderMockRule = MockedConstructionRule(ReportParamsAppender::class.java)

    @get:Rule
    val reportConfigProviderMockRule = MockedConstructionRule(LazyReportConfigProvider::class.java)

    @get:Rule
    val startupParamsAppenderMockRule = MockedConstructionRule(StartupParamsAppender::class.java)

    @Before
    fun setUp() {
        whenever(Utils.notIsBadRequestCondition()).thenReturn(notIsBadRequestCondition)
    }

    @Test
    fun createReportTask() {
        val result = NetworkTaskFactory.createReportTask(componentUnit)
        ObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .checkFieldIsInstanceOf("executor", BlockingExecutor::class.java)
            .checkFieldIsInstanceOf(
                "connectionBasedExecutionPolicy", ConnectionBasedExecutionPolicy::class.java
            )
            .checkFieldRecursively<ExponentialBackoffPolicy>("exponentialBackoffPolicy") {
                it
                    .withPrivateFields(true)
                    .withIgnoredFields("tag")
                    .checkField(
                        "exponentialBackoffDataHolder",
                        exponentialBackoffDataHolderMockedRule.constructionMock.constructed()[0]
                    ).also {
                        checkExponentialBackoffDataHolder(NetworkHost.REPORT)
                    }
                    .checkAll()
            }
            .checkFieldRecursively<ReportTask>("underlyingTask") { reportTaskAssertions ->
                reportTaskAssertions
                    .withPrivateFields(true)
                    .withIgnoredFields(
                        "mTrimmer", "mPublicLogger", "vitalComponentDataProvider", "mDbHelper",
                        "mSelfReporter", "mQueryValues", "sendingDataTaskHelper"
                    )
                    .checkField(
                        "fullUrlFormer",
                        fullUrlFormerMockedRule.constructionMock.constructed()[0]
                    ).also {
                        val urlFormerArguments = fullUrlFormerMockedRule.argumentInterceptor.flatArguments()
                        assertThat(urlFormerArguments).hasSize(2)
                        assertThat(urlFormerArguments[0]).isNotNull
                        assertThat(urlFormerArguments[1])
                            .isEqualTo(reportConfigProviderMockRule.constructionMock.constructed()[0])
                        assertThat(reportConfigProviderMockRule.argumentInterceptor.flatArguments())
                            .containsExactly(componentUnit)
                    }
                    .checkField(
                        "paramsAppender",
                        reportParamsAppenderMockRule.constructionMock.constructed()[0]
                    ).also {
                        val arguments = reportParamsAppenderMockRule.argumentInterceptor.flatArguments()
                        assertThat(arguments).hasSize(1)
                        assertThat(arguments[0])
                            .isNotNull
                            .isInstanceOfAny(AESRSARequestBodyEncrypter::class.java)
                    }
                    .checkField("mComponent", componentUnit)
                    .checkFieldsNonNull("requestDataHolder", "responseDataHolder")
                    .checkField("configProvider", reportConfigProviderMockRule.constructionMock.constructed()[0])
                    .checkAll()
            }
            .checkField("shouldTryNextHostConditions", listOf(notIsBadRequestCondition))
            .checkField("userAgent", customUserAgent)
            .checkAll()
    }

    @Test
    fun createStartupTask() {
        val finalConfigArgumentCaptor = ConstructionArgumentCaptor<FinalConfigProvider<*>> { mock, context ->
            whenever(mock.config).thenReturn(startupRequestConfig)
        }

        val finalConfigProviderMock = Mockito.mockConstruction(
            FinalConfigProvider::class.java,
            finalConfigArgumentCaptor
        )
        try {
            val result = NetworkTaskFactory.createStartupTask(startupUnit, startupRequestConfig)
            ObjectPropertyAssertions(result)
                .withPrivateFields(true)
                .checkFieldIsInstanceOf("executor", BlockingExecutor::class.java)
                .checkFieldIsInstanceOf(
                    "connectionBasedExecutionPolicy", ConnectionBasedExecutionPolicy::class.java
                )
                .checkFieldRecursively<ExponentialBackoffPolicy>("exponentialBackoffPolicy") { policyAssertions ->
                    policyAssertions
                        .withPrivateFields(true)
                        .withIgnoredFields("tag")
                        .checkField(
                            "exponentialBackoffDataHolder",
                            exponentialBackoffDataHolderMockedRule.constructionMock.constructed()[0]
                        ).also {
                            checkExponentialBackoffDataHolder(NetworkHost.STARTUP)
                        }
                        .checkAll()
                }
                .checkFieldRecursively<StartupTask>("underlyingTask") { startupTaskAssertions ->
                    startupTaskAssertions
                        .withPrivateFields(true)
                        .withIgnoredFields("responseHandler")
                        .checkField("mStartupUnit", startupUnit)
                        .checkField(
                            "fullUrlFormer",
                            fullUrlFormerMockedRule.constructionMock.constructed()[0]
                        ).also {
                            val urlFormerArguments = fullUrlFormerMockedRule.argumentInterceptor.flatArguments()
                            assertThat(urlFormerArguments).hasSize(2)
                            assertThat(urlFormerArguments[0]).isNotNull
                            assertThat(urlFormerArguments[1]).isEqualTo(finalConfigProviderMock.constructed()[0])
                            assertThat(finalConfigArgumentCaptor.flatArguments())
                                .containsExactly(startupRequestConfig)
                        }
                        .checkField("requestConfigProvider", finalConfigProviderMock.constructed()[0])
                        .checkFieldsNonNull("requestDataHolder", "responseDataHolder")
                        .checkAll()
                }
                .checkField("shouldTryNextHostConditions", emptyList<NetworkTask.ShouldTryNextHostCondition>())
                .checkField("userAgent", customUserAgent)
                .checkAll()
        } finally {
            finalConfigProviderMock.close()
        }
    }

    private fun checkExponentialBackoffDataHolder(networkHost: NetworkHost) {
        val holderArguments = exponentialBackoffDataHolderMockedRule.argumentInterceptor.flatArguments()
        assertThat(holderArguments).containsExactly(
            hostRetryProviderImplMockedRule.constructionMock.constructed()[0],
            networkHost.name
        )
        val hostRetryInfoProviderImplArguments = hostRetryProviderImplMockedRule.argumentInterceptor.flatArguments()
        assertThat(hostRetryInfoProviderImplArguments).containsExactly(
            GlobalServiceLocator.getInstance().servicePreferences,
            networkHost
        )
    }
}
