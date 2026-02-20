package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AdvIdentifiersFromIdentifierResultConverterTest extends CommonTest {

    private final AdvIdentifiersFromIdentifierResultConverter advIdentifiersConverter =
        new AdvIdentifiersFromIdentifierResultConverter();

    @Test
    public void testConvertNulls() {
        AdvIdentifiersResult result = advIdentifiersConverter.convert(null, null, null);
        assertThat(result).usingRecursiveComparison().isEqualTo(
            new AdvIdentifiersResult(
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null),
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null),
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null)
            )
        );
    }

    @Test
    public void testConvertOnlyGoogleNull() {
        final String hoaid = "hoaid";
        final String yandex = "yandex";
        final String errorExplanation = "error";
        AdvIdentifiersResult result = advIdentifiersConverter.convert(
            null,
            new IdentifiersResult(hoaid, IdentifierStatus.OK, errorExplanation),
            new IdentifiersResult(yandex, IdentifierStatus.OK, errorExplanation)
        );
        assertThat(result).usingRecursiveComparison().isEqualTo(
            new AdvIdentifiersResult(
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null),
                new AdvIdentifiersResult.AdvId(hoaid, AdvIdentifiersResult.Details.OK, errorExplanation),
                new AdvIdentifiersResult.AdvId(yandex, AdvIdentifiersResult.Details.OK, errorExplanation)
            )
        );
    }

    @Test
    public void testConvertOnlyHuaweiNull() {
        final String gaid = "gaid";
        final String yandex = "yandex";
        final String errorExplanation = "error";
        AdvIdentifiersResult result = advIdentifiersConverter.convert(
            new IdentifiersResult(gaid, IdentifierStatus.OK, errorExplanation),
            null,
            new IdentifiersResult(yandex, IdentifierStatus.OK, errorExplanation)
        );
        assertThat(result).usingRecursiveComparison().isEqualTo(
            new AdvIdentifiersResult(
                new AdvIdentifiersResult.AdvId(gaid, AdvIdentifiersResult.Details.OK, errorExplanation),
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null),
                new AdvIdentifiersResult.AdvId(yandex, AdvIdentifiersResult.Details.OK, errorExplanation)
            )
        );
    }

    @Test
    public void testConvertOnlyYandexNull() {
        final String gaid = "gaid";
        final String hoaid = "hoaid";
        final String errorExplanation = "error";
        AdvIdentifiersResult result = advIdentifiersConverter.convert(
            new IdentifiersResult(gaid, IdentifierStatus.OK, errorExplanation),
            new IdentifiersResult(hoaid, IdentifierStatus.OK, errorExplanation),
            null
        );
        assertThat(result).usingRecursiveComparison().isEqualTo(
            new AdvIdentifiersResult(
                new AdvIdentifiersResult.AdvId(gaid, AdvIdentifiersResult.Details.OK, errorExplanation),
                new AdvIdentifiersResult.AdvId(hoaid, AdvIdentifiersResult.Details.OK, errorExplanation),
                new AdvIdentifiersResult.AdvId(null, AdvIdentifiersResult.Details.INTERNAL_ERROR, null)
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
        AdvIdentifiersResult result = advIdentifiersConverter.convert(
            new IdentifiersResult(gaid, IdentifierStatus.OK, gaidError),
            new IdentifiersResult(hoaid, IdentifierStatus.FEATURE_DISABLED, hoaidError),
            new IdentifiersResult(yandex, IdentifierStatus.INVALID_ADV_ID, yandexError)
        );
        assertThat(result).usingRecursiveComparison().isEqualTo(
            new AdvIdentifiersResult(
                new AdvIdentifiersResult.AdvId(gaid, AdvIdentifiersResult.Details.OK, gaidError),
                new AdvIdentifiersResult.AdvId(hoaid, AdvIdentifiersResult.Details.FEATURE_DISABLED, hoaidError),
                new AdvIdentifiersResult.AdvId(yandex, AdvIdentifiersResult.Details.INVALID_ADV_ID, yandexError)
            )
        );
    }
}
