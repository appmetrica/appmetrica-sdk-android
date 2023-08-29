package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.AdsIdentifiersResult;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AdsIdentifiersFromIdentifierResultConverterStatusTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "For {0} is {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = Arrays.asList(new Object[][]{
                {IdentifierStatus.FEATURE_DISABLED, AdsIdentifiersResult.Details.FEATURE_DISABLED},
                {IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, AdsIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE},
                {IdentifierStatus.INVALID_ADV_ID, AdsIdentifiersResult.Details.INVALID_ADV_ID},
                {IdentifierStatus.NO_STARTUP, AdsIdentifiersResult.Details.NO_STARTUP},
                {IdentifierStatus.OK, AdsIdentifiersResult.Details.OK},
                {IdentifierStatus.UNKNOWN, AdsIdentifiersResult.Details.INTERNAL_ERROR}
        });
        assert data.size() == IdentifierStatus.values().length;
        assert data.size() ==  AdsIdentifiersResult.Details.values().length;
        return data;
    }

    public AdsIdentifiersFromIdentifierResultConverterStatusTest(IdentifierStatus internalStatus, AdsIdentifiersResult.Details expected) {
        mInternalStatus = internalStatus;
        mExpected = expected;
    }

    private final IdentifierStatus mInternalStatus;
    private final AdsIdentifiersResult.Details mExpected;

    @Test
    public void testConvertStatus() {
        assertThat(new AdsIdentifiersFromIdentifierResultConverter()
                .convert(null, new IdentifiersResult(null, mInternalStatus, null), null)
                .huaweiAdvId.details
        ).isEqualTo(mExpected);
    }
}
