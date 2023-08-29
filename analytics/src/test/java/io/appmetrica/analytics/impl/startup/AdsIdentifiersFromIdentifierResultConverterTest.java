package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.AdsIdentifiersResult;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AdsIdentifiersFromIdentifierResultConverterTest extends CommonTest {

    private AdsIdentifiersFromIdentifierResultConverter mAdsIdentifiersConverter = new AdsIdentifiersFromIdentifierResultConverter();

    @Test
    public void testConvertNulls() {
        AdsIdentifiersResult result = mAdsIdentifiersConverter.convert(null, null, null);
        assertThat(result).isEqualToComparingFieldByFieldRecursively(
                new AdsIdentifiersResult(
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null),
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null),
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null)
                )
        );
    }

    @Test
    public void testConvertOnlyGoogleNull() {
        final String hoaid = "hoaid";
        final String yandex = "yandex";
        final String errorExplanation = "error";
        AdsIdentifiersResult result = mAdsIdentifiersConverter.convert(
                null,
                new IdentifiersResult(hoaid, IdentifierStatus.OK, errorExplanation),
                new IdentifiersResult(yandex, IdentifierStatus.OK, errorExplanation)
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(
                new AdsIdentifiersResult(
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null),
                        new AdsIdentifiersResult.AdvId(hoaid, AdsIdentifiersResult.Details.OK, errorExplanation),
                        new AdsIdentifiersResult.AdvId(yandex, AdsIdentifiersResult.Details.OK, errorExplanation)
                )
        );
    }

    @Test
    public void testConvertOnlyHuaweiNull() {
        final String gaid = "gaid";
        final String yandex = "yandex";
        final String errorExplanation = "error";
        AdsIdentifiersResult result = mAdsIdentifiersConverter.convert(
                new IdentifiersResult(gaid, IdentifierStatus.OK, errorExplanation),
                null,
                new IdentifiersResult(yandex, IdentifierStatus.OK, errorExplanation)
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(
                new AdsIdentifiersResult(
                        new AdsIdentifiersResult.AdvId(gaid, AdsIdentifiersResult.Details.OK, errorExplanation),
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null),
                        new AdsIdentifiersResult.AdvId(yandex, AdsIdentifiersResult.Details.OK, errorExplanation)
                )
        );
    }

    @Test
    public void testConvertOnlyYandexNull() {
        final String gaid = "gaid";
        final String hoaid = "hoaid";
        final String errorExplanation = "error";
        AdsIdentifiersResult result = mAdsIdentifiersConverter.convert(
                new IdentifiersResult(gaid, IdentifierStatus.OK, errorExplanation),
                new IdentifiersResult(hoaid, IdentifierStatus.OK, errorExplanation),
                null
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(
                new AdsIdentifiersResult(
                        new AdsIdentifiersResult.AdvId(gaid, AdsIdentifiersResult.Details.OK, errorExplanation),
                        new AdsIdentifiersResult.AdvId(hoaid, AdsIdentifiersResult.Details.OK, errorExplanation),
                        new AdsIdentifiersResult.AdvId(null, AdsIdentifiersResult.Details.INTERNAL_ERROR, null)
                )
        );
    }

    @Test
    public void testConvertAllNonNull() {
        final String gaid = "gaid";
        final String hoaid = "hoaid";
        final String yandex = "yandex";
        final String gaidError = "gaid error";
        final String hoaidError = "hoaid error";
        final String yandexError = "yander error";
        AdsIdentifiersResult result = mAdsIdentifiersConverter.convert(
                new IdentifiersResult(gaid, IdentifierStatus.OK, gaidError),
                new IdentifiersResult(hoaid, IdentifierStatus.FEATURE_DISABLED, hoaidError),
                new IdentifiersResult(yandex, IdentifierStatus.INVALID_ADV_ID, yandexError)
        );
        assertThat(result).isEqualToComparingFieldByFieldRecursively(
                new AdsIdentifiersResult(
                        new AdsIdentifiersResult.AdvId(gaid, AdsIdentifiersResult.Details.OK, gaidError),
                        new AdsIdentifiersResult.AdvId(hoaid, AdsIdentifiersResult.Details.FEATURE_DISABLED, hoaidError),
                        new AdsIdentifiersResult.AdvId(yandex, AdsIdentifiersResult.Details.INVALID_ADV_ID, yandexError)
                )
        );
    }
}
