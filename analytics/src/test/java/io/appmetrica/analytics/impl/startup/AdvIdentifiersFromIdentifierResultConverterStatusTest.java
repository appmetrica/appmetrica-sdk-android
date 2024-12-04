package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class AdvIdentifiersFromIdentifierResultConverterStatusTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "For {0} is {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = Arrays.asList(new Object[][]{
            {IdentifierStatus.FEATURE_DISABLED, AdvIdentifiersResult.Details.FEATURE_DISABLED},
            {IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, AdvIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE},
            {IdentifierStatus.INVALID_ADV_ID, AdvIdentifiersResult.Details.INVALID_ADV_ID},
            {IdentifierStatus.NO_STARTUP, AdvIdentifiersResult.Details.NO_STARTUP},
            {IdentifierStatus.OK, AdvIdentifiersResult.Details.OK},
            {IdentifierStatus.UNKNOWN, AdvIdentifiersResult.Details.INTERNAL_ERROR},
            {IdentifierStatus.INVALID_ADV_ID, AdvIdentifiersResult.Details.INVALID_ADV_ID}
        });
        assert data.size() == IdentifierStatus.values().length;
        assert data.size() == AdvIdentifiersResult.Details.values().length;
        return data;
    }

    public AdvIdentifiersFromIdentifierResultConverterStatusTest(IdentifierStatus internalStatus, AdvIdentifiersResult.Details expected) {
        mInternalStatus = internalStatus;
        mExpected = expected;
    }

    private final IdentifierStatus mInternalStatus;
    private final AdvIdentifiersResult.Details mExpected;

    @Test
    public void testConvertStatus() {
        assertThat(new AdvIdentifiersFromIdentifierResultConverter()
            .convert(null, new IdentifiersResult(null, mInternalStatus, null), null)
            .huaweiAdvId.details
        ).isEqualTo(mExpected);
    }
}
