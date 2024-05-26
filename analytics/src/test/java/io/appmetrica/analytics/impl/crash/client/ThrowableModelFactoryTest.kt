package io.appmetrica.analytics.impl.crash.client

import android.os.Build
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.AndroidUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.ExecutionException

@RunWith(RobolectricTestRunner::class)
class ThrowableModelFactoryTest : CommonTest() {

    @Mock
    private lateinit var throwable: IllegalStateException
    @Rule
    @JvmField
    val sUtils = MockedStaticRule(Utils::class.java)
    @Rule
    @JvmField
    val sAndroidUtilsMockedRule = MockedStaticRule(AndroidUtils::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT)).thenReturn(true)
    }

    @Test
    fun createModelNullable() {
        `when`(Utils.getStackTraceSafely(throwable)).thenReturn(emptyArray())
        `when`(throwable.suppressed).thenReturn(emptyArray())
        ObjectPropertyAssertions(ThrowableModelFactory.createModel(throwable))
            .checkField("exceptionClass", "getExceptionClass", "java.lang.IllegalStateException")
            .checkField("stacktrace", "getStacktrace", emptyList<StackTraceItemInternal>())
            .checkField("suppressed", "getSuppressed", emptyList<ThrowableModel>())
            .checkFieldsAreNull("message", "cause")
            .checkAll()
    }

    @Test
    fun createModelFilled() {
        val cause = mock<RuntimeException>()
        val cause2 = mock<NumberFormatException>()
        val cause3 = mock<NullPointerException>()
        val suppressed1 = mock<InterruptedException>()
        val suppressed2 = mock<ExecutionException>()
        val suppressed3 = mock<IllegalArgumentException>()
        val suppressed4 = mock<InterruptedException>()
        val message = "some message"
        val causeMessage = "some cause message"
        val cause2Message = "some cause 2 message"
        val cause3Message = "some cause 3 message"
        val suppressed1Message = "some suppressed 1 message"
        val suppressed2Message = "some suppressed 2 message"
        val suppressed3Message = "some suppressed 3 message"
        val suppressed4Message = "some suppressed 4 message"
        `when`(throwable.message).thenReturn(message)
        `when`(throwable.cause).thenReturn(cause)
        `when`(throwable.suppressed).thenReturn(arrayOf(suppressed1, suppressed2))
        `when`(cause.message).thenReturn(causeMessage)
        `when`(cause.cause).thenReturn(cause3)
        `when`(cause.suppressed).thenReturn(arrayOf(suppressed3))
        `when`(cause2.message).thenReturn(cause2Message)
        `when`(cause2.cause).thenReturn(null)
        `when`(cause2.suppressed).thenReturn(emptyArray())
        `when`(cause3.message).thenReturn(cause3Message)
        `when`(cause3.suppressed).thenReturn(emptyArray())
        `when`(suppressed1.message).thenReturn(suppressed1Message)
        `when`(suppressed2.message).thenReturn(suppressed2Message)
        `when`(suppressed3.message).thenReturn(suppressed3Message)
        `when`(suppressed4.message).thenReturn(suppressed4Message)
        `when`(suppressed1.cause).thenReturn(cause2)
        `when`(suppressed2.cause).thenReturn(null)
        `when`(suppressed4.cause).thenReturn(null)
        `when`(suppressed1.suppressed).thenReturn(emptyArray())
        `when`(suppressed2.suppressed).thenReturn(arrayOf(suppressed4))
        `when`(suppressed4.suppressed).thenReturn(emptyArray())
        `when`(Utils.getStackTraceSafely(any())).thenReturn(emptyArray())
        `when`(Utils.getStackTraceSafely(throwable)).thenReturn(Array(3) { StackTraceElement("class$it", "method$it", "file$it", it * 10) })
        `when`(Utils.getStackTraceSafely(cause)).thenReturn(Array(2) { StackTraceElement("class${it + 3}", "method${it + 3}", "file${it + 3}", (it + 3) * 10) })
        `when`(Utils.getStackTraceSafely(cause3)).thenReturn(Array(2) { StackTraceElement("class${it + 5}", "method${it + 5}", "file${it + 5}", (it + 5) * 10) })
        `when`(Utils.getStackTraceSafely(suppressed3)).thenReturn(Array(2) { StackTraceElement("class${it + 7}", "method${it + 7}", "file${it + 7}", (it + 7) * 10) })
        `when`(Utils.getStackTraceSafely(suppressed1)).thenReturn(Array(2) { StackTraceElement("class${it + 9}", "method${it + 9}", "file${it + 9}", (it + 9) * 10) })
        `when`(Utils.getStackTraceSafely(suppressed2)).thenReturn(Array(2) { StackTraceElement("class${it + 11}", "method${it + 11}", "file${it + 11}", (it + 11) * 10) })
        `when`(Utils.getStackTraceSafely(cause2)).thenReturn(Array(2) { StackTraceElement("class${it + 13}", "method${it + 13}", "file${it + 13}", (it + 13) * 10) })
        `when`(Utils.getStackTraceSafely(suppressed4)).thenReturn(Array(2) { StackTraceElement("class${it + 15}", "method${it + 15}", "file${it + 15}", (it + 15) * 10) })

        val actual = ThrowableModelFactory.createModel(throwable)
        ObjectPropertyAssertions(actual)
            .withIgnoredFields("suppressed")
            .checkField("exceptionClass", "getExceptionClass", "java.lang.IllegalStateException")
            .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                StackTraceItemInternal("class0", "file0", 0, null, "method0", false),
                StackTraceItemInternal("class1", "file1", 10, null, "method1", false),
                StackTraceItemInternal("class2", "file2", 20, null, "method2", false),
            ))
            .checkField("message", "getMessage", message)
            .checkFieldRecursively<ThrowableModel>("cause") {
                it
                    .withPrivateFields(true)
                    .withIgnoredFields("suppressed")
                    .checkField("exceptionClass", "getExceptionClass", "java.lang.RuntimeException")
                    .checkField("message", "getMessage", causeMessage)
                    .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                        StackTraceItemInternal("class3", "file3", 30, null, "method3", false),
                        StackTraceItemInternal("class4", "file4", 40, null, "method4", false)
                    ))
                    .checkFieldRecursively<ThrowableModel>("cause") {
                        it
                            .withPrivateFields(true)
                            .checkField("exceptionClass", "getExceptionClass", "java.lang.NullPointerException")
                            .checkField("message", "getMessage", cause3Message)
                            .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                                StackTraceItemInternal("class5", "file5", 50, null, "method5", false),
                                StackTraceItemInternal("class6", "file6", 60, null, "method6", false)
                            ))
                            .checkField("suppressed", emptyList<ThrowableModel>())
                            .checkFieldsAreNull("cause")

                    }
                assertThat(it.actual.suppressed!!.size).isEqualTo(1)
                ObjectPropertyAssertions(it.actual.suppressed!![0])
                    .checkField("exceptionClass", "getExceptionClass", "java.lang.IllegalArgumentException")
                    .checkField("message", "getMessage", suppressed3Message)
                    .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                        StackTraceItemInternal("class7", "file7", 70, null, "method7", false),
                        StackTraceItemInternal("class8", "file8", 80, null, "method8", false)
                    ))
                    .checkFieldsAreNull("cause","suppressed")
                    .checkAll()
            }
            .checkAll()
        assertThat(actual.suppressed!!.size).isEqualTo(2)
        ObjectPropertyAssertions(actual.suppressed[0])
            .checkField("exceptionClass", "getExceptionClass", "java.lang.InterruptedException")
            .checkField("message", "getMessage", suppressed1Message)
            .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                StackTraceItemInternal("class9", "file9", 90, null, "method9", false),
                StackTraceItemInternal("class10", "file10", 100, null, "method10", false)
            ))
            .checkField("suppressed", "getSuppressed", emptyList<ThrowableModel>())
            .checkFieldRecursively<ThrowableModel>("cause") {
                it
                    .withPrivateFields(true)
                    .checkField("exceptionClass", "getExceptionClass", "java.lang.NumberFormatException")
                    .checkField("message", "getMessage", cause2Message)
                    .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                        StackTraceItemInternal("class13", "file13", 130, null, "method13", false),
                        StackTraceItemInternal("class14", "file14", 140, null, "method14", false)
                    ))
                    .checkFieldIsNull("cause")
                    .checkField("suppressed", emptyList<ThrowableModel>())
            }
            .checkAll()
        ObjectPropertyAssertions(actual.suppressed[1])
            .withIgnoredFields("suppressed")
            .checkField("exceptionClass", "getExceptionClass", "java.util.concurrent.ExecutionException")
            .checkField("message", "getMessage", suppressed2Message)
            .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                StackTraceItemInternal("class11", "file11", 110, null, "method11", false),
                StackTraceItemInternal("class12", "file12", 120, null, "method12", false)
            ))
            .checkFieldIsNull("cause")
            .checkAll()
        assertThat(actual.suppressed[1].suppressed!!.size).isEqualTo(1)
        ObjectPropertyAssertions(actual.suppressed[1].suppressed!![0])
            .checkField("exceptionClass", "getExceptionClass", "java.lang.InterruptedException")
            .checkField("message", "getMessage", suppressed4Message)
            .checkFieldComparingFieldByFieldRecursively("stacktrace", "getStacktrace", listOf(
                StackTraceItemInternal("class15", "file15", 150, null, "method15", false),
                StackTraceItemInternal("class16", "file16", 160, null, "method16", false)
            ))
            .checkFieldIsNull("cause")
            .checkField("suppressed", emptyList<ThrowableModel>())
            .checkAll()
    }

    @Test
    fun createModelCauseRecursionDepthIsLessThanLimit() {
        `when`(Utils.getStackTraceSafely(any())).thenReturn(emptyArray())
        val mainThrowable = mock<RuntimeException>()
        var currentThrowable = mainThrowable
        repeat(30) {
            val cause = mock<IllegalStateException>()
            `when`(currentThrowable.cause).thenReturn(cause)
            `when`(currentThrowable.suppressed).thenReturn(emptyArray())
            currentThrowable = cause
        }
        val result = ThrowableModelFactory.createModel(mainThrowable)
        var currentModelThrowable: ThrowableModel? = result
        repeat(30) {
            assertThat(currentModelThrowable!!.cause).isNotNull
            currentModelThrowable = currentModelThrowable!!.cause
        }
        assertThat(currentModelThrowable!!.cause).isNull()
    }

    @Test
    fun createModelCauseRecursionDepthIsGreaterThanLimit() {
        `when`(Utils.getStackTraceSafely(any())).thenReturn(emptyArray())
        val mainThrowable = mock<RuntimeException>()
        var currentThrowable = mainThrowable
        repeat(31) {
            val cause = mock<IllegalStateException>()
            `when`(currentThrowable.cause).thenReturn(cause)
            `when`(currentThrowable.suppressed).thenReturn(emptyArray())
            currentThrowable = cause
        }
        val result = ThrowableModelFactory.createModel(mainThrowable)
        var currentModelThrowable: ThrowableModel? = result
        repeat(30) {
            assertThat(currentModelThrowable!!.cause).isNotNull
            currentModelThrowable = currentModelThrowable!!.cause
        }
        assertThat(currentModelThrowable!!.cause).isNull()
    }
}
