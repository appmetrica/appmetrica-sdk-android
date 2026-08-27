package io.appmetrica.analytics.impl;

import io.appmetrica.gradle.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.assertions.ObjectPropertyAssertions;

public class DeferredDeeplinkStateTest extends CommonTest {

    @Test
    public void constructorNullable() throws Exception {
        DeferredDeeplinkState state = new DeferredDeeplinkState(null, null, null);
        ObjectPropertyAssertions<DeferredDeeplinkState> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(state);
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
        ObjectPropertyAssertions<DeferredDeeplinkState> assertions = Assertions.INSTANCE.ObjectPropertyAssertions(state);
        assertions.checkField("mDeeplink", deeplink);
        assertions.checkField("mParameters", parameters);
        assertions.checkField("mUnparsedReferrer", referrer);
        assertions.checkAll();
    }
}
