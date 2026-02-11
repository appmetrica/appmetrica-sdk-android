package io.appmetrica.analytics.impl.startup.uuid

import io.appmetrica.analytics.coreutils.internal.io.FileUtils.resetSdkStorage
import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.analytics.impl.db.FileConstants
import io.appmetrica.analytics.impl.utils.UuidGenerator
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.ContextRule
import io.appmetrica.analytics.testutils.LogRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

internal class PersistentUuidHolderTest : CommonTest() {

    @get:Rule
    val contextRule = ContextRule()
    private val context by contextRule

    private val uuidGenerator: UuidGenerator = mock()

    private lateinit var uuidFile: File
    private lateinit var oldUuidFile: File
    private val fileName = FileConstants.UUID_FILE_NAME

    private val knownUuid = UUID.randomUUID().toString()
    private val generatedUuid = UUID.randomUUID().toString()
    private val oldUuid = UUID.randomUUID().toString()
    private val invalidUuid = "Invalid uuid"

    private val uuidValidator: UuidValidator = mock {
        on { isValid(knownUuid) } doReturn true
        on { isValid(null) } doReturn false
        on { isValid(invalidUuid) } doReturn false
    }

    private val storageDir: File by lazy { context.noBackupFilesDir }

    private lateinit var persistentUuidHolder: PersistentUuidHolder

    @get:Rule
    val logRule = LogRule()

    @Before
    fun setUp() {
        resetSdkStorage()
        val sdkDir = File(storageDir, "/appmetrica/analytics")
        sdkDir.mkdirs()
        uuidFile = File(sdkDir, FileConstants.UUID_FILE_NAME)
        oldUuidFile = File(storageDir, FileConstants.UUID_FILE_NAME)
        whenever(uuidGenerator.generateUuid()).thenReturn(generatedUuid)
        persistentUuidHolder = PersistentUuidHolder(context, uuidGenerator, uuidValidator)
    }

    @After
    fun tearDown() {
        resetSdkStorage()
        uuidFile.delete()
        storageDir.delete()
        oldUuidFile.delete()
    }

    @Test
    fun readUuid() {
        val uuid = UUID.randomUUID().toString()
        IOUtils.writeStringFileLocked(uuid, fileName, FileOutputStream(uuidFile))
        assertThat(persistentUuidHolder.readUuid()).isEqualTo(uuid)
    }

    @Test
    fun readUuidIfFileIsNull() {
        assertThat(persistentUuidHolder.readUuid()).isNull()
    }

    @Test
    fun handleUuid() {
        assertThat(persistentUuidHolder.handleUuid(null)).isEqualTo(generatedUuid)
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isEqualTo(generatedUuid)
    }

    @Test
    fun handleInvalidUuid() {
        assertThat(persistentUuidHolder.handleUuid(invalidUuid)).isEqualTo(generatedUuid)
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isEqualTo(generatedUuid)
    }

    @Test
    fun handleUuidNullFile() {
        uuidFile.delete()
        assertThat(persistentUuidHolder.handleUuid(null)).isEqualTo(generatedUuid)
        uuidFile.delete()
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isNull()
    }

    @Test
    fun handleUuidWithKnownUuid() {
        assertThat(persistentUuidHolder.handleUuid(knownUuid)).isEqualTo(knownUuid)
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isEqualTo(knownUuid)
    }

    @Test
    fun handleUuidWithKnownUuidNullFile() {
        assertThat(persistentUuidHolder.handleUuid(knownUuid)).isEqualTo(knownUuid)
        uuidFile.delete()
        assertThat(IOUtils.getStringFileLocked(uuidFile)).isNull()
    }

    @Test
    fun handleUuidWithoutKnownUuidIfThrown() {
        whenever(uuidGenerator.generateUuid()).thenThrow(RuntimeException::class.java)
        assertThat(persistentUuidHolder.handleUuid(null)).isNull()
    }

    @Test
    fun checkMigrationIfOldAndActualFilesExists() {
        persistentUuidHolder.handleUuid(knownUuid)
        oldUuidFile.writeText(oldUuid)
        persistentUuidHolder.checkMigration()
        assertThat(persistentUuidHolder.readUuid()).isEqualTo(knownUuid)
    }

    @Test
    fun checkMigrationIfOnlyOldFileExists() {
        oldUuidFile.writeText(oldUuid)
        persistentUuidHolder.checkMigration()
        assertThat(persistentUuidHolder.readUuid()).isEqualTo(oldUuid)
    }

    @Test
    fun checkMigrationIfOldAndNewFilesAreMissing() {
        persistentUuidHolder.checkMigration()
        assertThat(persistentUuidHolder.readUuid()).isNull()
    }
}
