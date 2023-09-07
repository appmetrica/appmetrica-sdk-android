package io.appmetrica.analytics.impl.service.migration

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.DatabaseStorage
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.impl.protobuf.client.LegacyStartupStateProtobuf
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.utils.encryption.AESCredentialProvider
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.LogRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.constructionRule
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceMigrationScriptToV112Test : CommonTest() {

    private val database = mock<SQLiteDatabase>()
    private val startupStateCaptor = argumentCaptor<StartupState>()

    @get:Rule
    val globalServiceLocatorRule = GlobalServiceLocatorRule()

    private val password = "password".toByteArray()
    private val iv = "iv".toByteArray()
    private val startupStateEncryptedValue = "startup state encrypted value".toByteArray()
    private val legacyStartupStateEncryptedValue = "legacy startup state encrypted value".toByteArray()

    @get:Rule
    val aesCredentialProviderMockedConstructionRule =
        MockedConstructionRule(AESCredentialProvider::class.java) { mock, _ ->
            whenever(mock.password).thenReturn(password)
            whenever(mock.iv).thenReturn(iv)
        }

    @get:Rule
    val aesEncrypterMockedConstructionRule = MockedConstructionRule(AESEncrypter::class.java) {mock, _ ->
        whenever(mock.encrypt(any())).thenReturn(startupStateEncryptedValue)
    }

    private val databaseStorage = mock<DatabaseStorage> {
        on { readableDatabase } doReturn database
    }
    private val databaseStorageFactory = mock<DatabaseStorageFactory> {
        on { storageForService } doReturn databaseStorage
    }
    @get:Rule
    val databaseMockedStaticRule = staticRule<DatabaseStorageFactory>()

    @get:Rule
    val startupStateStorageMockedConstructionRule = constructionRule<StartupState.Storage>()

    @get:Rule
    val logRule = LogRule()

    private val uuid = "uuid"
    private val deviceID = "deviceID"
    private val deviceIDHash = "deviceIDHash"
    private val countryInit = "countryInit"

    private var cursor: Cursor? = null
    private lateinit var context: Context

    private lateinit var serviceMigrationScriptToV112: ServiceMigrationScriptToV112
    private lateinit var aesCredentialProvider: AESCredentialProvider
    private lateinit var aesEncrypter: AESEncrypter

    @Before
    fun setUp() {
        context = GlobalServiceLocator.getInstance().context
        whenever(DatabaseStorageFactory.getInstance(context)).thenReturn(databaseStorageFactory)
        serviceMigrationScriptToV112 = ServiceMigrationScriptToV112()
        aesCredentialProvider = aesCredentialProvider()
        aesEncrypter = aesEncrypter()
    }

    @After
    fun tearDowm() {
        cursor?.close()
    }

    @Test
    fun `runScript for valid legacy data`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.uuid = uuid
        legacyStartupState.deviceId = deviceID
        legacyStartupState.deviceIdHash = deviceIDHash
        legacyStartupState.countryInit = countryInit
        legacyStartupState.hadFirstStartup = true
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "countryInit", "hadFirstStartup")
            .checkField("uuid", uuid)
            .checkField("deviceId", deviceID)
            .checkField("deviceIdHash", deviceIDHash)
            .checkField("countryInit", countryInit)
            .checkField("hadFirstStartup", true)
            .checkAll()
    }

    @Test
    fun `runScript for legacy data without uuid`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.deviceId = deviceID
        legacyStartupState.deviceIdHash = deviceIDHash
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash")
            .checkField("deviceId", deviceID)
            .checkField("deviceIdHash", deviceIDHash)
            .checkFieldIsNull("uuid")
            .checkAll()
    }

    @Test
    fun `runScript for legacy data without deviceID`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.uuid = uuid
        legacyStartupState.deviceIdHash = deviceIDHash
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash")
            .checkField("uuid", uuid)
            .checkField("deviceIdHash", deviceIDHash)
            .checkFieldIsNull("deviceId")
            .checkAll()
    }

    @Test
    fun `runScript for legacy data without deviceIDHash`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.uuid = uuid
        legacyStartupState.deviceId = deviceID
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash")
            .checkField("uuid", uuid)
            .checkField("deviceId", deviceID)
            .checkFieldIsNull("deviceIdHash")
            .checkAll()
    }

    @Test
    fun `runScript for false hadFirstStartup`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.hadFirstStartup = false
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        ProtoObjectPropertyAssertions(interceptStartupState())
            .withPrivateFields(true)
            .withPermittedFields("hadFirstStartup")
            .checkField("hadFirstStartup", false)
            .checkAll()
    }

    @Test
    fun `runScript for empty legacy data`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash")
            .checkAll()
    }

    @Test
    fun `runScript for legacy data with empty bytes`() {
        stubDatabaseWithLegacyStartupState(ByteArray(0))

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash")
            .checkAll()
    }

    @Test
    fun `runScript for missing legacy data`() {
        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash", "countryInit")
            .checkAll()
    }

    @Test
    fun `runScript for wrong legacy data`() {
        stubDatabaseWithLegacyStartupState("wrong value".toByteArray())
        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash", "countryInit")
            .checkAll()
    }

    @Test
    fun `runScript for null cursor`() {
        whenever(
            database.query(
                eq(Constants.BinaryDataTable.TABLE_NAME),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(null)

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash", "countryInit")
            .checkAll()
    }

    @Test
    fun `runScript for read exception`() {
        whenever(
            database.query(
                eq(Constants.BinaryDataTable.TABLE_NAME),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        ).thenThrow(RuntimeException("Some exception"))

        serviceMigrationScriptToV112.run(context)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPrivateFields(true)
            .withPermittedFields("uuid", "deviceId", "deviceIdHash", "hadFirstStartup", "countryInit")
            .checkField("hadFirstStartup", false)
            .checkFieldsAreNull("uuid", "deviceId", "deviceIdHash", "countryInit")
            .checkAll()
    }

    private fun aesEncrypter(): AESEncrypter {
        assertThat(aesEncrypterMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(aesEncrypterMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(AESEncrypter.DEFAULT_ALGORITHM, password, iv)
        return aesEncrypterMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun aesCredentialProvider(): AESCredentialProvider {
        assertThat(aesCredentialProviderMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(aesCredentialProviderMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        return aesCredentialProviderMockedConstructionRule.constructionMock.constructed().first()
    }

    private fun interceptStartupState(): StartupState {
        verify(startupStorage()).save(startupStateCaptor.capture())
        assertThat(startupStateCaptor.allValues).hasSize(1)
        return startupStateCaptor.firstValue
    }

    private fun stubDatabaseWithLegacyStartupState(
        startupStateProtobuf: LegacyStartupStateProtobuf.LegacyStartupState
    ) {
        stubDatabaseWithLegacyStartupState(MessageNano.toByteArray(startupStateProtobuf))
    }

    private fun stubDatabaseWithLegacyStartupState(value: ByteArray) {
        whenever(aesEncrypter.decrypt(legacyStartupStateEncryptedValue)).thenReturn(value)
        whenever(
            database.query(
                eq(Constants.BinaryDataTable.TABLE_NAME),
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(
            MatrixCursor(arrayOf(Constants.BinaryDataTable.VALUE)).apply {
                addRow(arrayOf(legacyStartupStateEncryptedValue))
            }
        )
    }

    private fun startupStorage(): StartupState.Storage {
        assertThat(startupStateStorageMockedConstructionRule.constructionMock.constructed()).hasSize(1)
        assertThat(startupStateStorageMockedConstructionRule.argumentInterceptor.flatArguments())
            .containsExactly(context)
        return startupStateStorageMockedConstructionRule.constructionMock.constructed().first()
    }
}
