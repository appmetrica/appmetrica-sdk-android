package io.appmetrica.analytics.impl.crash.client.converter

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.crash.client.StackTraceItemInternal
import io.appmetrica.analytics.impl.crash.client.ThreadState
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

class ThreadStateConverterTest : CommonTest() {

    @Mock
    private lateinit var stackTraceConverter: StackTraceConverter
    @Rule
    @JvmField
    val sUtils = MockedStaticRule(Utils::class.java)
    @Rule
    @JvmField
    val sCollectionUtils = MockedStaticRule(CollectionUtils::class.java)
    private lateinit var converter: ThreadStateConverter

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(CollectionUtils.unmodifiableListCopy(any<List<StackTraceItemInternal>>())).thenCallRealMethod()
        converter = ThreadStateConverter(stackTraceConverter)
    }

    @Test
    fun toProtoFilled() {
        val name = "some name"
        val priority = 55
        val tid = 33L
        val group = "some group"
        val state = 77

        val inputStacktrace = listOf(mock<StackTraceElement>())
        val internalStacktrace = listOf(mock<StackTraceItemInternal>())
        val convertedStacktrace = arrayOf(mock<CrashAndroid.StackTraceElement>())
        `when`(Utils.convertStackTraceToInternal(inputStacktrace)).thenReturn(internalStacktrace)
        `when`(stackTraceConverter.fromModel(internalStacktrace)).thenReturn(convertedStacktrace)

        val model = ThreadState(name, priority, tid, group, state, inputStacktrace)

        ProtoObjectPropertyAssertions(converter.fromModel(model))
            .checkField("name", name)
            .checkField("priority", priority)
            .checkField("tid", tid)
            .checkField("group", group)
            .checkField("state", state)
            .checkField("stacktrace", convertedStacktrace)
            .checkAll()
    }

    @Test
    fun toProtoWithNullable() {
        val name = "some name"
        val priority = 55
        val tid = 33L
        val group = "some group"

        val model = ThreadState(name, priority, tid, group, null, null)

        ProtoObjectPropertyAssertions(converter.fromModel(model))
            .checkField("name", name)
            .checkField("priority", priority)
            .checkField("tid", tid)
            .checkField("group", group)
            .checkField("state", -1)
            .checkFieldIsNull("stacktrace")
            .checkAll()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun toModel() {
        converter.toModel(CrashAndroid.Thread())
    }
}
