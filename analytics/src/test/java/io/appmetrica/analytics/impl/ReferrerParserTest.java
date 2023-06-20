package io.appmetrica.analytics.impl;

import android.net.Uri;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class ReferrerParserTest extends CommonTest {

    private final ReferrerParser mReferrerParser = new ReferrerParser();

    @Test
    public void nullReferrer() throws Exception {
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(null);
        checkState(state, null, null, null);
    }

    @Test
    public void noGetParametersInReferrer() throws Exception {
        String referrerWithoutGetParameters = "no_params";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithoutGetParameters);
        checkState(state, null, null, referrerWithoutGetParameters);
    }

    @Test
    public void emptyQueryParametersInReferrer() throws Exception {
        String referrerWithEmptyQueryParameters = "referrer?";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithEmptyQueryParameters);
        checkState(state, null, null, referrerWithEmptyQueryParameters);
    }

    @Test
    public void invalidQueryParameters() throws Exception {
        String referrerWithInvalidParameters = "referrer?invalid_param";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithInvalidParameters);
        checkState(state, null, null, referrerWithInvalidParameters);
    }

    @Test
    public void irrelevantParameters() throws Exception {
        String referrerWithSeveralIrrelevantParameters = "referrer?key0=value0%key1=value1";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithSeveralIrrelevantParameters);
        checkState(state, null, null, referrerWithSeveralIrrelevantParameters);
    }

    @Test
    public void emptyDeeplink() throws Exception {
        String referrerWithEmptyDeeplink = "referrer?appmetrica_deep_link=";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithEmptyDeeplink);
        checkState(state, "", null, referrerWithEmptyDeeplink);
    }

    @Test
    public void emptyDeeplinkAndOtherParameters() throws Exception {
        String referrerWithEmptyDeeplinkAndOtherParameters = "referrer?appmetrica_deep_link=&key0=value0";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithEmptyDeeplinkAndOtherParameters);
        checkState(state, "", null, referrerWithEmptyDeeplinkAndOtherParameters);
    }

    @Test
    public void notEmptyDeeplinkNoParameters() throws Exception {
        String referrerWithDeeplink = "referrer?appmetrica_deep_link=some_deeplink";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplink);
        checkState(state, "some_deeplink", new HashMap<String, String>(), referrerWithDeeplink);
    }

    @Test
    public void notEmptyDeeplinkNoParametersAndOtherParameters() throws Exception {
        String referrerWithDeeplinkAndOtherParameters = "referrer?appmetrica_deep_link=some_deeplink&key0=value0";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndOtherParameters);
        checkState(state, "some_deeplink", new HashMap<String, String>(), referrerWithDeeplinkAndOtherParameters);
    }

    @Test
    public void notEmptyDeeplinkWithParameter() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key0", "value0");
        String referrerWithDeeplinkAndParameter = "referrer?appmetrica_deep_link=some_deeplink%3Fkey0%3Dvalue0";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndParameter);
        checkState(state, "some_deeplink?key0=value0", parameters, referrerWithDeeplinkAndParameter);
    }

    @Test
    public void notEmptyDeeplinkWithParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key0", "value0");
        parameters.put("key1", "value1");
        String referrerWithDeeplinkAndParameter = "referrer?appmetrica_deep_link=some_deeplink%3Fkey0%3Dvalue0%26key1%3Dvalue1";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndParameter);
        checkState(state, "some_deeplink?key0=value0&key1=value1", parameters, referrerWithDeeplinkAndParameter);
    }

    @Test
    public void notEmptyDeeplinkWithParametersAndOtherParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key0", "value0");
        parameters.put("key1", "value1");
        String referrerWithDeeplinkAndParameter = "referrer?appmetrica_deep_link=some_deeplink%3Fkey0%3Dvalue0%26key1%3Dvalue1&key2=value2";
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndParameter);
        checkState(state, "some_deeplink?key0=value0&key1=value1", parameters, referrerWithDeeplinkAndParameter);
    }

    @Test
    public void notEmptyDeeplinkWithAnotherDeeplinkAsParameter() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        String deeplinkAsParameter = "appmetricasample://path_in_app?key2=value2&key3=value3";
        String deeplinkAsParameterEncoded = Uri.encode(deeplinkAsParameter);
        String deeplink = "some_deeplink?key0=value0&key1=" + deeplinkAsParameterEncoded;
        String deeplinkEncoded = Uri.encode(deeplink);
        parameters.put("key0", "value0");
        parameters.put("key1", deeplinkAsParameter);
        String referrerWithDeeplinkAndParameter = "referrer?appmetrica_deep_link=" + deeplinkEncoded;
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndParameter);
        checkState(state, deeplink, parameters, referrerWithDeeplinkAndParameter);
    }

    @Test
    public void deeplinkWithBothValidAndInvalidParams() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("utm_source", "google");
        parameters.put("utm_medium", "cpc");
        parameters.put("utm_term", "running");
        parameters.put("shoes", "");
        parameters.put("utm_content", "logolink");
        parameters.put("utm_campaign", "spring_sale");

        String deeplink = "utm_source=google" +
                "&utm_medium=cpc" +
                "&utm_term=running&shoes" +
                "&utm_content=logolink" +
                "&utm_campaign=spring_sale";
        String referrerWithDeeplinkAndParameter = "referrer?appmetrica_deep_link=" + Uri.encode(deeplink);
        DeferredDeeplinkState state = mReferrerParser.parseDeferredDeeplinkState(referrerWithDeeplinkAndParameter);
        checkState(state, deeplink, parameters, referrerWithDeeplinkAndParameter);
    }

    private void checkState(DeferredDeeplinkState state,
                            String expectedDeeplink,
                            Map<String, String> expectedParameters,
                            String expectedReferrer) throws Exception {
        ObjectPropertyAssertions<DeferredDeeplinkState> assertions = ObjectPropertyAssertions(state);
        assertions.checkField("mDeeplink", expectedDeeplink);
        assertions.checkField("mParameters", expectedParameters);
        assertions.checkField("mUnparsedReferrer", expectedReferrer);
        assertions.checkAll();
    }
}
