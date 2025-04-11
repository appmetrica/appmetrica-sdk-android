package io.appmetrica.analytics.impl.utils.concurrency

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileLocksHolderTest : CommonTest() {

    @get:Rule
    val fileUtilsMockedStaticRule = MockedStaticRule(FileUtils::class.java)

    private val firstName = "file1"
    private val secondName = "file2"

    private val context: Context = mock()
    private val firstFile: File = mock()
    private val secondFile: File = mock()

    private lateinit var fileLocksHolder: FileLocksHolder

    @Before
    fun setUp() {
        whenever(FileUtils.getFileFromSdkStorage(context, "$firstName.lock")).thenReturn(firstFile)
        whenever(FileUtils.getFileFromSdkStorage(context, "$secondName.lock")).thenReturn(secondFile)
        fileLocksHolder = FileLocksHolder(context)
    }

    @Test
    fun getInstance() {
        val instance = FileLocksHolder.getInstance(context)
        assertThat(instance).isNotNull
        assertThat(FileLocksHolder.getInstance(context)).isSameAs(instance)
    }

    @Test
    fun getOrCreate() {
        val lock1 = fileLocksHolder.getOrCreate(firstName)
        assertThat(lock1).isNotNull
        val lock2 = fileLocksHolder.getOrCreate(secondName)
        assertThat(lock2).isNotNull
        assertThat(fileLocksHolder.getOrCreate(firstName)).isSameAs(lock1)
        assertThat(fileLocksHolder.getOrCreate(secondName)).isSameAs(lock2)
    }
}
