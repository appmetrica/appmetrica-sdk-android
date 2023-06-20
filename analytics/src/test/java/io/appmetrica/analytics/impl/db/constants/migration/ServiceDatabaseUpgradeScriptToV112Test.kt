package io.appmetrica.analytics.impl.db.constants.migration

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.encryption.AESEncrypter
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.db.constants.Constants
import io.appmetrica.analytics.impl.db.constants.migrations.ServiceDatabaseUpgradeScriptToV112
import io.appmetrica.analytics.impl.protobuf.client.LegacyStartupStateProtobuf
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf
import io.appmetrica.analytics.impl.utils.encryption.AESCredentialProvider
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.MockedConstructionRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceDatabaseUpgradeScriptToV112Test : CommonTest() {

    private val database = mock<SQLiteDatabase>()
    private val contentValueCaptor = argumentCaptor<ContentValues>()
    private val bytesArgumentCaptor = argumentCaptor<ByteArray>()

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

    private val uuid = "uuid"
    private val deviceID = "deviceID"
    private val deviceIDHash = "deviceIDHash"
    private val countryInit = "countryInit"

    private var cursor: Cursor? = null
    private lateinit var context: Context

    private lateinit var serviceDatabaseUpgradeScriptToV112: ServiceDatabaseUpgradeScriptToV112
    private lateinit var aesCredentialProvider: AESCredentialProvider
    private lateinit var aesEncrypter: AESEncrypter

    @Before
    fun setUp() {
        serviceDatabaseUpgradeScriptToV112 = ServiceDatabaseUpgradeScriptToV112()
        context = GlobalServiceLocator.getInstance().context
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

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "countryInit", "hadFirstStartup")
            .checkField("uuid", uuid)
            .checkField("deviceID", deviceID)
            .checkField("deviceIDHash", deviceIDHash)
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

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash")
            .checkField("uuid", "")
            .checkField("deviceID", deviceID)
            .checkField("deviceIDHash", deviceIDHash)
            .checkAll()
    }

    @Test
    fun `runScript for legacy data without deviceID`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.uuid = uuid
        legacyStartupState.deviceIdHash = deviceIDHash
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash")
            .checkField("uuid", uuid)
            .checkField("deviceID", "")
            .checkField("deviceIDHash", deviceIDHash)
            .checkAll()
    }

    @Test
    fun `runScript for legacy data without deviceIDHash`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.uuid = uuid
        legacyStartupState.deviceId = deviceID
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash")
            .checkField("uuid", uuid)
            .checkField("deviceID", deviceID)
            .checkField("deviceIDHash", "")
            .checkAll()
    }

    @Test
    fun `runScript for false hadFirstStartup`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        legacyStartupState.hadFirstStartup = false
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        ProtoObjectPropertyAssertions(interceptStartupState())
            .withPermittedFields("hadFirstStartup")
            .checkField("hadFirstStartup", false)
            .checkAll()
    }

    @Test
    fun `runScript for empty legacy data`() {
        val legacyStartupState = LegacyStartupStateProtobuf.LegacyStartupState()
        stubDatabaseWithLegacyStartupState(legacyStartupState)

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkAll()
    }

    @Test
    fun `runScript for legacy data with empty bytes`() {
        stubDatabaseWithLegacyStartupState(ByteArray(0))

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkAll()
    }

    @Test
    fun `runScript for missing legacy data`() {
        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkAll()
    }

    @Test
    fun `runScript for wrong legacy data`() {
        stubDatabaseWithLegacyStartupState("wrong value".toByteArray())
        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
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

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
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

        serviceDatabaseUpgradeScriptToV112.runScript(database)

        val result = interceptStartupState()

        ProtoObjectPropertyAssertions(result)
            .withPermittedFields("uuid", "deviceID", "deviceIDHash", "hadFirstStartup", "countryInit")
            .checkField("uuid", "")
            .checkField("deviceID", "")
            .checkField("deviceIDHash", "")
            .checkField("hadFirstStartup", false)
            .checkField("countryInit", "")
            .checkAll()
    }

    @Test
    fun `runScript for write exception`() {
        stubDatabaseWithLegacyStartupState(LegacyStartupStateProtobuf.LegacyStartupState())

        whenever(
            database.insertWithOnConflict(
                eq(Constants.BinaryDataTable.TABLE_NAME),
                eq(null),
                contentValueCaptor.capture(),
                eq(SQLiteDatabase.CONFLICT_REPLACE)
            )
        ).thenThrow(RuntimeException())

        serviceDatabaseUpgradeScriptToV112.runScript(database)
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

    private fun interceptStartupState(): StartupStateProtobuf.StartupState? {
        verify(database).insertWithOnConflict(
            eq(Constants.BinaryDataTable.TABLE_NAME),
            eq(null),
            contentValueCaptor.capture(),
            eq(SQLiteDatabase.CONFLICT_REPLACE)
        )
        assertThat(contentValueCaptor.allValues).hasSize(1)

        val contentValues = contentValueCaptor.firstValue
        assertThat(contentValues.get(Constants.BinaryDataTable.DATA_KEY)).isEqualTo("startup_state")
        assertThat(contentValues.getAsByteArray(Constants.BinaryDataTable.VALUE)).isEqualTo(startupStateEncryptedValue)

        verify(aesEncrypter).encrypt(bytesArgumentCaptor.capture())

        assertThat(bytesArgumentCaptor.allValues).hasSize(1)
        return StartupStateProtobuf.StartupState.parseFrom(bytesArgumentCaptor.firstValue)
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
}
