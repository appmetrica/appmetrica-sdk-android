package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class StackTraceElementConverterTest extends CommonTest {

    private final StackTraceElementConverter elementConverter = new StackTraceElementConverter();

    @Test
    public void testToProto() throws IllegalAccessException {
        String className = "some class";
        String fileName = "some file";
        String methodName = "some method";
        int line = 22;
        int column = 44;
        StackTraceItemInternal stackTraceElement = new StackTraceItemInternal(
            className,
            fileName,
            line,
            column,
            methodName,
            true
        );
        ObjectPropertyAssertions<CrashAndroid.StackTraceElement> assertions = ObjectPropertyAssertions(
            elementConverter.fromModel(stackTraceElement)
        );

        assertions.withFinalFieldOnly(false)
            .checkField("className", className)
            .checkField("fileName", fileName)
            .checkField("lineNumber", line)
            .checkField("columnNumber", column)
            .checkField("methodName", methodName)
            .checkField("isNative", true)
            .checkAll();
    }

    @Test
    public void testToProtoWithNulls() throws IllegalAccessException {
        StackTraceItemInternal stackTraceElement = new StackTraceItemInternal(
            null,
            null,
            null,
            null,
            null,
            null
        );
        ObjectPropertyAssertions<CrashAndroid.StackTraceElement> assertions = ObjectPropertyAssertions(
            elementConverter.fromModel(stackTraceElement)
        );

        assertions.withFinalFieldOnly(false)
            .checkField("className", "")
            .checkField("fileName", "")
            .checkField("lineNumber", -1)
            .checkField("columnNumber", -1)
            .checkField("methodName", "")
            .checkField("isNative", false)
            .checkAll();

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        elementConverter.toModel(new CrashAndroid.StackTraceElement());
    }

}
