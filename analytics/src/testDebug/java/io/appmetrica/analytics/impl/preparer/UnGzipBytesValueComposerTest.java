package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Rule;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

/**
 * @see UnGzipBytesValueComposerGetValueTest
 */
public class UnGzipBytesValueComposerTest extends CommonTest {

    @Rule
    public final GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    @Test
    public void constructor() throws Exception {
        UnGzipBytesValueComposer composer = new UnGzipBytesValueComposer();

        ObjectPropertyAssertions<UnGzipBytesValueComposer> assertions =
            Assertions.INSTANCE.ObjectPropertyAssertions(composer)
                .withDeclaredAccessibleFields(true);

        assertions.checkFieldNonNull("eventEncrypterProvider", "getEventEncrypterProvider");

        assertions.checkAll();
    }
}
