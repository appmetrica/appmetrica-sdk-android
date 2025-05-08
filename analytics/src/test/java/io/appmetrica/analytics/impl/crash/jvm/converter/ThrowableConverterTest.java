package io.appmetrica.analytics.impl.crash.jvm.converter;

import android.os.Build;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ThrowableConverterTest extends CommonTest {

    @Mock
    private StackTraceConverter backtraceConverter;
    @InjectMocks
    private ThrowableConverter throwableConverter = new ThrowableConverter();
    @Rule
    public final MockedStaticRule<AndroidUtils> sUtils = new MockedStaticRule<>(AndroidUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        throwableConverter = new ThrowableConverter(backtraceConverter);
        when(AndroidUtils.isApiAchieved(Build.VERSION_CODES.KITKAT)).thenCallRealMethod();
    }

    @Test
    public void toProto() throws IllegalAccessException {
        String mainExceptionClass = "some class";
        String mainMessage = "some message";
        List<StackTraceItemInternal> mainStacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        CrashAndroid.StackTraceElement[] mainProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(mainStacktrace)).thenReturn(mainProtoStacktrace);

        final String cause1ExceptionClass = "some class cause1";
        final String cause1Message = "some message cause1";
        List<StackTraceItemInternal> cause1Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        final CrashAndroid.StackTraceElement[] cause1ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(cause1Stacktrace)).thenReturn(cause1ProtoStacktrace);

        final String cause2ExceptionClass = "some class cause2";
        final String cause2Message = "some message cause2";
        List<StackTraceItemInternal> cause2Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        final CrashAndroid.StackTraceElement[] cause2ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(cause2Stacktrace)).thenReturn(cause2ProtoStacktrace);

        final String cause3ExceptionClass = "some class cause3";
        final String cause3Message = "some message cause3";
        List<StackTraceItemInternal> cause3Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        final CrashAndroid.StackTraceElement[] cause3ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(cause3Stacktrace)).thenReturn(cause3ProtoStacktrace);

        String suppressed1ExceptionClass = "some class suppressed1";
        String suppressed1Message = "some message suppressed1";
        List<StackTraceItemInternal> suppressed1Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        CrashAndroid.StackTraceElement[] suppressed1ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(suppressed1Stacktrace)).thenReturn(suppressed1ProtoStacktrace);

        String suppressed2ExceptionClass = "some class suppressed2";
        String suppressed2Message = "some message suppressed2";
        List<StackTraceItemInternal> suppressed2Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        CrashAndroid.StackTraceElement[] suppressed2ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(suppressed2Stacktrace)).thenReturn(suppressed2ProtoStacktrace);

        final String suppressed3ExceptionClass = "some class suppressed3";
        final String suppressed3Message = "some message suppressed3";
        List<StackTraceItemInternal> suppressed3Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        final CrashAndroid.StackTraceElement[] suppressed3ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(suppressed3Stacktrace)).thenReturn(suppressed3ProtoStacktrace);

        String suppressed4ExceptionClass = "some class suppressed4";
        String suppressed4Message = "some message suppressed4";
        List<StackTraceItemInternal> suppressed4Stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        CrashAndroid.StackTraceElement[] suppressed4ProtoStacktrace = new CrashAndroid.StackTraceElement[]{mock(CrashAndroid.StackTraceElement.class)};
        when(backtraceConverter.fromModel(suppressed4Stacktrace)).thenReturn(suppressed4ProtoStacktrace);

        ThrowableModel mainModel = new ThrowableModel(
            mainExceptionClass,
            mainMessage,
            mainStacktrace,
            new ThrowableModel(
                cause1ExceptionClass,
                cause1Message,
                cause1Stacktrace,
                new ThrowableModel(cause2ExceptionClass, cause2Message, cause2Stacktrace, null, null),
                Collections.singletonList(new ThrowableModel(suppressed3ExceptionClass, suppressed3Message, suppressed3Stacktrace, null, null))
            ),
            Arrays.asList(
                new ThrowableModel(
                    suppressed1ExceptionClass,
                    suppressed1Message,
                    suppressed1Stacktrace,
                    new ThrowableModel(cause3ExceptionClass, cause3Message, cause3Stacktrace, null, null),
                    null
                ),
                new ThrowableModel(
                    suppressed2ExceptionClass,
                    suppressed2Message,
                    suppressed2Stacktrace,
                    null,
                    Collections.singletonList(new ThrowableModel(suppressed4ExceptionClass, suppressed4Message, suppressed4Stacktrace, null, null))
                )
            )
        );
        CrashAndroid.Throwable actual = throwableConverter.fromModel(mainModel);
        new ProtoObjectPropertyAssertions<>(actual)
            .withIgnoredFields("suppressed")
            .checkField("exceptionClass", mainExceptionClass)
            .checkField("message", mainMessage)
            .checkField("backtrace", mainProtoStacktrace)
            .checkFieldRecursively("cause", new Consumer<ObjectPropertyAssertions<CrashAndroid.Throwable>>() {
                @Override
                public void accept(ObjectPropertyAssertions<CrashAndroid.Throwable> assertions) {
                    assertions
                        .withIgnoredFields("suppressed")
                        .checkField("exceptionClass", cause1ExceptionClass)
                        .checkField("message", cause1Message)
                        .checkField("backtrace", cause1ProtoStacktrace)
                        .checkFieldRecursively("cause", new Consumer<ObjectPropertyAssertions<CrashAndroid.Throwable>>() {
                            @Override
                            public void accept(ObjectPropertyAssertions<CrashAndroid.Throwable> assertions) {
                                assertions
                                    .checkField("exceptionClass", cause2ExceptionClass)
                                    .checkField("message", cause2Message)
                                    .checkField("backtrace", cause2ProtoStacktrace)
                                    .checkField("suppressed", new CrashAndroid.Throwable[0])
                                    .checkFieldIsNull("cause");
                            }
                        });
                    CrashAndroid.Throwable[] suppressed = assertions.getActual().suppressed;
                    assertThat(suppressed.length).isEqualTo(1);
                    new ProtoObjectPropertyAssertions<>(suppressed[0])
                        .checkField("exceptionClass", suppressed3ExceptionClass)
                        .checkField("message", suppressed3Message)
                        .checkField("backtrace", suppressed3ProtoStacktrace)
                        .checkField("suppressed", new CrashAndroid.Throwable[0])
                        .checkFieldIsNull("cause")
                        .checkAll();
                }
            })
            .checkAll();
        assertThat(actual.suppressed.length).isEqualTo(2);
        new ProtoObjectPropertyAssertions<>(actual.suppressed[0])
            .checkField("exceptionClass", suppressed1ExceptionClass)
            .checkField("message", suppressed1Message)
            .checkField("backtrace", suppressed1ProtoStacktrace)
            .checkField("suppressed", new CrashAndroid.Throwable[0])
            .checkFieldRecursively("cause", new Consumer<ObjectPropertyAssertions<CrashAndroid.Throwable>>() {
                @Override
                public void accept(ObjectPropertyAssertions<CrashAndroid.Throwable> assertions) {
                    assertions
                        .checkField("exceptionClass", cause3ExceptionClass)
                        .checkField("message", cause3Message)
                        .checkField("backtrace", cause3ProtoStacktrace)
                        .checkField("suppressed", new CrashAndroid.Throwable[0])
                        .checkFieldIsNull("cause");
                }
            })
            .checkAll();
        new ProtoObjectPropertyAssertions<>(actual.suppressed[1])
            .withIgnoredFields("suppressed")
            .checkField("exceptionClass", suppressed2ExceptionClass)
            .checkField("message", suppressed2Message)
            .checkField("backtrace", suppressed2ProtoStacktrace)
            .checkFieldIsNull("cause")
            .checkAll();
        assertThat(actual.suppressed[1].suppressed.length).isEqualTo(1);
        new ProtoObjectPropertyAssertions<>(actual.suppressed[1].suppressed[0])
            .checkField("exceptionClass", suppressed4ExceptionClass)
            .checkField("message", suppressed4Message)
            .checkField("backtrace", suppressed4ProtoStacktrace)
            .checkField("suppressed", new CrashAndroid.Throwable[0])
            .checkFieldIsNull("cause")
            .checkAll();
    }

    @Test
    public void toProtoWithNullable() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ThrowableModel model = new ThrowableModel(null, null, null, null, null);
        new ProtoObjectPropertyAssertions<>(throwableConverter.fromModel(model))
            .checkField("exceptionClass", "")
            .checkField("message", "")
            .checkField("suppressed", new CrashAndroid.Throwable[0])
            .checkField("backtrace", new CrashAndroid.StackTraceElement[0])
            .checkFieldsAreNull("cause")
            .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        throwableConverter.toModel(new CrashAndroid.Throwable());
    }

    @Test
    public void messageWithInvalidEncoding() throws Throwable {
        String prefix = "identifier";
        String invalidFormattedStringWithUnpairedSurrogate = "\uD83D";
        ThrowableModel throwableModel = new ThrowableModel(
            "some class",
            prefix + invalidFormattedStringWithUnpairedSurrogate,
            null,
            null,
            null
        );
        CrashAndroid.Throwable proto = throwableConverter.fromModel(throwableModel);
        byte[] protoBytes = MessageNano.toByteArray(proto);
        assertThat(CrashAndroid.Throwable.parseFrom(protoBytes).message).contains(prefix);
    }
}
