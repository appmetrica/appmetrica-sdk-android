package io.appmetrica.analytics.impl.db.session

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.ServerTime
import io.appmetrica.analytics.impl.utils.TimeUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.function.Consumer

@RunWith(RobolectricTestRunner::class)
class DbSessionModelFactoryTest : CommonTest() {

    private val deviceId = "Test device id"
    private val uuid = "Test uuid"
    private val appVersion = "App version"
    private val appBuildNumber = "App build number"
    private val kitBuildNumber = "Kit build number"
    private val kitBuildType = "Kit build type"
    private val osVersion = "Os version"
    private val osApiLevel = 123213
    private val analyticsSdkVersionName = "Analytics sdk version name"
    private val locale = "Test locale"
    private val rootStatus = "Root status"
    private val debuggable = "Debuggable"
    private val appFramework = "App framework"
    private val attributionId = 3423
    private val commitHash = "qwertyasdfgh"
    private val expectedJson = "{" +
        "\"dId\":\"$deviceId\"," +
        "\"uId\":\"$uuid\"," +
        "\"appVer\":\"$appVersion\"," +
        "\"appBuild\":\"$appBuildNumber\"," +
        "\"kitBuildType\":\"$kitBuildType\"," +
        "\"osVer\":\"$osVersion\"," +
        "\"osApiLev\":$osApiLevel," +
        "\"lang\":\"$locale\"," +
        "\"root\":\"$rootStatus\"," +
        "\"app_debuggable\":\"$debuggable\"," +
        "\"app_framework\":\"$appFramework\"," +
        "\"attribution_id\":$attributionId," +
        "\"analyticsSdkVersionName\":\"$analyticsSdkVersionName\"," +
        "\"kitBuildNumber\":\"$kitBuildNumber\"" +
        "}"

    private val reportRequestConfig: ReportRequestConfig = mock {
        on { deviceId } doReturn deviceId
        on { uuid } doReturn uuid
        on { appVersion } doReturn appVersion
        on { appBuildNumber } doReturn appBuildNumber
        on { analyticsSdkBuildNumber } doReturn kitBuildNumber
        on { analyticsSdkBuildType } doReturn kitBuildType
        on { osVersion } doReturn osVersion
        on { osApiLevel } doReturn osApiLevel
        on { analyticsSdkVersionName } doReturn analyticsSdkVersionName
        on { locale } doReturn locale
        on { deviceRootStatus } doReturn rootStatus
        on { isAppDebuggable } doReturn debuggable
        on { appFramework } doReturn appFramework
        on { attributionId } doReturn attributionId
    }

    private val id = 42L
    private val type = SessionType.BACKGROUND
    private val startTime = 424242L
    private val serverTimeOffset = 1234L

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val timeUtilsRule = MockedStaticRule(TimeUtils::class.java)

    @Before
    fun setUp() {
        ServerTime.getInstance().init()
        whenever(TimeUtils.getServerTimeOffset()).thenReturn(serverTimeOffset)
    }

    @Test
    fun build() {
        val model = DbSessionModelFactory(
            reportRequestConfig,
            id,
            type,
            startTime
        ).create()

        ObjectPropertyAssertions(model)
            .checkField("id", id)
            .checkField("type", type)
            .checkFieldRecursively("description", Consumer<ObjectPropertyAssertions<DbSessionModel.Description>> {
                it
                    .checkField("startTime", startTime)
                    .checkField("serverTimeOffset", serverTimeOffset)
                    .checkField("obtainedBeforeFirstSynchronization", ServerTime.getInstance().isUncheckedTime)
            })
            .checkField("reportRequestParameters", expectedJson)
            .checkAll()
    }
}
