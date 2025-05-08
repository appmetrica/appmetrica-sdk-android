package io.appmetrica.analytics.impl.db.event

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.AppEnvironment
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventSource
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.FirstOccurrenceStatus
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.PhoneUtils
import io.appmetrica.analytics.impl.component.EventNumberGenerator
import io.appmetrica.analytics.impl.component.session.SessionState
import io.appmetrica.analytics.impl.component.session.SessionType
import io.appmetrica.analytics.impl.db.state.converter.EventExtrasConverter
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.utils.encryption.EncryptedCounterReport
import io.appmetrica.analytics.impl.utils.encryption.EventEncryptionMode
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.function.Consumer

@RunWith(RobolectricTestRunner::class)
class DbEventModelFactoryTest : CommonTest() {

    private val sessionId = 42L
    private val sessionType = SessionType.BACKGROUND
    private val reportId = 4242L
    private val reportTime = 100500L
    private val globalNumber = 123321L
    private val numberOfType = 32L
    private val type = 12
    private val customType = 13
    private val name = "name string"
    private val value = "value string"
    private val eventEnvironment = "eventEnvironment string"
    private val bytesTruncated = 4212
    private val profileId = "profileId string"
    private val firstOccurrenceStatus = FirstOccurrenceStatus.FIRST_OCCURRENCE
    private val source = EventSource.JS
    private val attributionIdChanged = true
    private val openId = 4335
    private val extras = mutableMapOf(
        "key" to "value".toByteArray()
    )
    private val reportData: CounterReport = mock {
        on { type } doReturn type
        on { customType } doReturn customType
        on { name } doReturn name
        on { value } doReturn value
        on { eventEnvironment } doReturn eventEnvironment
        on { bytesTruncated } doReturn bytesTruncated
        on { profileID } doReturn profileId
        on { firstOccurrenceStatus } doReturn firstOccurrenceStatus
        on { source } doReturn source
        on { attributionIdChanged } doReturn attributionIdChanged
        on { openId } doReturn openId
        on { extras } doReturn extras
    }
    private val eventEncryptionMode = EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER
    private val environmentRevisionValue = "environmentRevisionValue string"
    private val revisionNumber = 14432L
    private val convertedExtras = "convertedExtras".toByteArray()
    private val connectionTypeInServerFormat = 2323
    private val internalEventsType = InternalEvents.EVENT_TYPE_ACTIVATION
    private val locationInfo: DbLocationModel = mock()

    private val context: Context = mock()
    private val sessionState: SessionState = mock {
        on { sessionId } doReturn sessionId
        on { sessionType } doReturn sessionType
        on { reportId } doReturn reportId
        on { reportTime } doReturn reportTime
    }
    private val reportType = 42
    private val eventNumberGenerator: EventNumberGenerator = mock {
        on { eventGlobalNumberAndGenerateNext } doReturn globalNumber
        on { getEventNumberOfTypeAndGenerateNext(reportType) } doReturn numberOfType
    }
    private val encryptedCounterReport = EncryptedCounterReport(reportData, eventEncryptionMode)
    private val reportRequestConfig: ReportRequestConfig = mock()
    private val environmentRevision = AppEnvironment.EnvironmentRevision(environmentRevisionValue, revisionNumber)
    private val eventExtrasConverter: EventExtrasConverter = mock {
        on { fromModel(extras) } doReturn convertedExtras
    }
    private val dbLocationModelFactory: DbLocationModelFactory = mock {
        on { create() } doReturn locationInfo
    }

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    @get:Rule
    val phoneUtilsRule = MockedStaticRule(PhoneUtils::class.java)

    @get:Rule
    val eventsManagerRule = MockedStaticRule(EventsManager::class.java)

    @get:Rule
    val internalEventsRule = MockedStaticRule(InternalEvents::class.java)

    private val factory = DbEventModelFactory(
        context,
        sessionState,
        reportType,
        eventNumberGenerator,
        encryptedCounterReport,
        reportRequestConfig,
        environmentRevision,
        eventExtrasConverter,
        dbLocationModelFactory
    )

    @Before
    fun setUp() {
        whenever(PhoneUtils.getConnectionTypeInServerFormat(context)).thenReturn(connectionTypeInServerFormat)
        whenever(EventsManager.shouldGenerateGlobalNumber(reportType)).thenReturn(true)
        whenever(InternalEvents.valueOf(type)).thenReturn(internalEventsType)
    }

    @Test
    fun build() {
        val model = factory.create()

        ObjectPropertyAssertions(model)
            .checkField("session", sessionId)
            .checkField("sessionType", sessionType)
            .checkField("numberInSession", reportId)
            .checkField("type", internalEventsType)
            .checkField("globalNumber", globalNumber)
            .checkField("time", reportTime)
            .checkFieldRecursively(
                "description",
                Consumer<ObjectPropertyAssertions<DbEventModel.Description>> {
                    it
                        .withIgnoredFields("cellularConnectionType")
                        .checkField("customType", customType)
                        .checkField("name", name)
                        .checkField("value", value)
                        .checkField("numberOfType", numberOfType)
                        .checkField("locationInfo", locationInfo)
                        .checkField("errorEnvironment", eventEnvironment)
                        .checkField("appEnvironment", environmentRevisionValue)
                        .checkField("appEnvironmentRevision", revisionNumber)
                        .checkField("truncated", bytesTruncated)
                        .checkField("connectionType", connectionTypeInServerFormat)
                        .checkField("encryptingMode", eventEncryptionMode)
                        .checkField("profileId", profileId)
                        .checkField("firstOccurrenceStatus", firstOccurrenceStatus)
                        .checkField("source", source)
                        .checkField("attributionIdChanged", attributionIdChanged)
                        .checkField("openId", openId)
                        .checkField("extras", convertedExtras)
                }
            )
            .checkAll()
    }

    @Test
    fun buildIfShouldNotGenerateGlobalNumber() {
        whenever(EventsManager.shouldGenerateGlobalNumber(any())).thenReturn(false)

        val model = factory.create()

        assertThat(model.globalNumber).isEqualTo(0)
    }
}
