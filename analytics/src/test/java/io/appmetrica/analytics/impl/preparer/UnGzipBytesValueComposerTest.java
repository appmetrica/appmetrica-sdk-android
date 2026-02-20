package io.appmetrica.analytics.impl.preparer;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Rule;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

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
            ObjectPropertyAssertions(composer)
                .withDeclaredAccessibleFields(true);

        assertions.checkFieldNonNull("eventEncrypterProvider", "getEventEncrypterProvider");

        assertions.checkAll();
    }
}
