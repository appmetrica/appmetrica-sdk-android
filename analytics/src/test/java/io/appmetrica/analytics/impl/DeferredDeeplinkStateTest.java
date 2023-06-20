package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class DeferredDeeplinkStateTest extends CommonTest {

    @Test
    public void constructorNullable() throws Exception {
        DeferredDeeplinkState state = new DeferredDeeplinkState(null, null, null);
        ObjectPropertyAssertions<DeferredDeeplinkState> assertions = ObjectPropertyAssertions(state);
        assertions.checkField("mDeeplink", (String) null);
        assertions.checkField("mParameters", (Map) null);
        assertions.checkField("mUnparsedReferrer", (String) null);
        assertions.checkAll();
    }

    @Test
    public void constructorFilled() throws Exception {
        String deeplink = "test_deeplink";
        String referrer = "test_referrer";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key0", "value0");
        parameters.put("key1", "value1");
        DeferredDeeplinkState state = new DeferredDeeplinkState(deeplink, parameters, referrer);
        ObjectPropertyAssertions<DeferredDeeplinkState> assertions = ObjectPropertyAssertions(state);
        assertions.checkField("mDeeplink", deeplink);
        assertions.checkField("mParameters", parameters);
        assertions.checkField("mUnparsedReferrer", referrer);
        assertions.checkAll();
    }
}
