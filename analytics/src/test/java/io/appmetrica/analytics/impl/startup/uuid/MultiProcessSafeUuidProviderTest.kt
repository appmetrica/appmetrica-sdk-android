package io.appmetrica.analytics.impl.startup.uuid

import android.content.Context
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.impl.utils.concurrency.ExclusiveMultiProcessFileLock
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.constructionRule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.notNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

internal class MultiProcessSafeUuidProviderTest : CommonTest() {

    @get:Rule
    val exclusiveMultiProcessFileLockMockedConstructionRule = constructionRule<ExclusiveMultiProcessFileLock>()

    private val storedUuid = UUID.randomUUID().toString()
    private val generatedUuid = UUID.randomUUID().toString()
    private val uuidFromOuterSource = UUID.randomUUID().toString()
    private val invalidUuid = "invalid"

    private val context: Context = mock()

    private val outerSourceUuidImporter: IOuterSourceUuidImporter = mock {
        on { get(context) } doReturn uuidFromOuterSource
    }

    private val lock: ExclusiveMultiProcessFileLock = mock()

    private val persistentUuidHolder: PersistentUuidHolder = mock {
        on { readUuid() } doReturn storedUuid
        on { handleUuid(notNull()) } doAnswer { it.getArgument(0) as String }
        on { handleUuid(null) } doReturn generatedUuid
    }

    private val uuidValidator: UuidValidator = mock {
        on { isValid(storedUuid) } doReturn true
        on { isValid(generatedUuid) } doReturn true
        on { isValid(uuidFromOuterSource) } doReturn true
        on { isValid(null) } doReturn false
        on { isValid(invalidUuid) } doReturn false
    }

    private val multiProcessSafeUuidProvider: MultiProcessSafeUuidProvider by setUp {
        MultiProcessSafeUuidProvider(context, outerSourceUuidImporter, lock, persistentUuidHolder, uuidValidator)
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MultiProcessUuidLockProvider.reset()
    }

    @After
    fun tearDown() {
        MultiProcessUuidLockProvider.reset()
    }

    @Test
    fun touchMigrationOnCreate() {
        inOrder(lock, persistentUuidHolder) {
            verify(lock).lock()
            verify(persistentUuidHolder).checkMigration()
            verify(lock).unlock()
        }
    }

    @Test
    fun readUuidIfExistsInPersistentProviderAfterLock() {
        whenever(persistentUuidHolder.readUuid()).thenReturn(storedUuid)
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), storedUuid)
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), storedUuid)
        inOrder(lock, persistentUuidHolder, outerSourceUuidImporter) {
            verify(lock).lock()
            verify(persistentUuidHolder).readUuid()
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun readUuidIfExistsInOuterSourceImporter() {
        clearInvocations(lock, persistentUuidHolder)
        whenever(persistentUuidHolder.readUuid()).thenReturn(null)
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), uuidFromOuterSource)
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), uuidFromOuterSource)
        inOrder(persistentUuidHolder, lock, outerSourceUuidImporter) {
            verify(lock).lock()
            verify(persistentUuidHolder).readUuid()
            verify(outerSourceUuidImporter).get(context)
            verify(persistentUuidHolder).handleUuid(uuidFromOuterSource)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `readUuid if stored is invalid`() {
        whenever(persistentUuidHolder.readUuid()).thenReturn(invalidUuid)
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), uuidFromOuterSource)
    }

    @Test
    fun readUuidIfGenerationPreconditionCheckerMatchesPreconditions() {
        whenever(persistentUuidHolder.readUuid()).thenReturn(null)
        whenever(outerSourceUuidImporter.get(context)).thenReturn(null)
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), generatedUuid)
        // Check memory cache
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), generatedUuid)
        inOrder(
            persistentUuidHolder,
            lock,
            outerSourceUuidImporter,
            persistentUuidHolder
        ) {
            verify(lock).lock()
            verify(persistentUuidHolder).readUuid()
            verify(outerSourceUuidImporter).get(context)
            verify(persistentUuidHolder).handleUuid(null)
            verify(lock).unlock()
            verifyNoMoreInteractions()
        }
    }

    @Test
    fun `readUuid if stored and imported are invalid`() {
        whenever(persistentUuidHolder.readUuid()).thenReturn(invalidUuid)
        whenever(outerSourceUuidImporter.get(context)).thenReturn(invalidUuid)
        whenever(persistentUuidHolder.handleUuid(invalidUuid)).thenReturn(generatedUuid)
        assertValidUuid(multiProcessSafeUuidProvider.readUuid(), generatedUuid)
    }

    @Test
    fun `readUuid if all uuid are invalid`() {
        whenever(persistentUuidHolder.readUuid()).thenReturn(invalidUuid)
        whenever(outerSourceUuidImporter.get(context)).thenReturn(invalidUuid)
        whenever(persistentUuidHolder.handleUuid(invalidUuid)).thenReturn(invalidUuid)

        ObjectPropertyAssertions(multiProcessSafeUuidProvider.readUuid())
            .checkFieldIsNull("id")
            .checkField("status", IdentifierStatus.UNKNOWN)
            .checkFieldMatchPredicate<String>("errorExplanation") {
                it.startsWith("Uuid must be obtained via async API AppMetrica#requestStartupParams")
            }.checkAll()
    }

    @Test
    fun exclusiveLockCreation() {
        val multiProcessUuidLockProvider = MultiProcessSafeUuidProvider(context, outerSourceUuidImporter)
        MultiProcessSafeUuidProvider(context, outerSourceUuidImporter)
        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.constructionMock.constructed())
            .hasSize(1)
        assertThat(exclusiveMultiProcessFileLockMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context, FileConstants.UUID_FILE_NAME)
        multiProcessUuidLockProvider.readUuid()

        // 2 times during each MultiProcessSafeUuidProvider creation and one more - during reading uuid
        verify(
            exclusiveMultiProcessFileLockMockedConstructionRule.constructionMock.constructed().first(),
            times(3)
        ).lock()
    }

    private fun assertValidUuid(result: IdentifiersResult, expected: String) {
        assertThat(result).isNotNull()
        SoftAssertions().apply {
            assertThat(result.id).`as`("Uuid").isEqualTo(expected)
            assertThat(result.status).`as`("Status").isEqualTo(IdentifierStatus.OK)
            assertThat(result.errorExplanation).`as`("Error").isNull()
            assertAll()
        }
    }
}
