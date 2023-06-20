package io.appmetrica.analytics;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AdsIdentifiersResultTest extends CommonTest {

    @Test
    public void testConstructor() throws Exception {
        AdsIdentifiersResult.AdvId gaid = mock(AdsIdentifiersResult.AdvId.class);
        AdsIdentifiersResult.AdvId hoaid = mock(AdsIdentifiersResult.AdvId.class);
        AdsIdentifiersResult.AdvId yandex = mock(AdsIdentifiersResult.AdvId.class);
        AdsIdentifiersResult result = new AdsIdentifiersResult(gaid, hoaid, yandex);
        ObjectPropertyAssertions<AdsIdentifiersResult> assertions = ObjectPropertyAssertions(result);
        assertions.checkField("googleAdvId", gaid);
        assertions.checkField("huaweiAdvId", hoaid);
        assertions.checkField("yandexAdvId", yandex);
        assertions.checkAll();
    }
}
