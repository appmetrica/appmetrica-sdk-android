package io.appmetrica.analytics.coreapi.identifiers;

import androidx.annotation.NonNull;
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
public class IdentifierStatusTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "For {0} is {1}")
    public static Collection<Object[]> data() {
        List<Object[]> data = Arrays.asList(new Object[][]{
            {IdentifierStatus.FEATURE_DISABLED, "FEATURE_DISABLED"},
            {IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, "IDENTIFIER_PROVIDER_UNAVAILABLE"},
            {IdentifierStatus.INVALID_ADV_ID, "INVALID_ADV_ID"},
            {IdentifierStatus.NO_STARTUP, "NO_STARTUP"},
            {IdentifierStatus.OK, "OK"},
            {IdentifierStatus.UNKNOWN, "UNKNOWN"},
            {IdentifierStatus.FORBIDDEN_BY_CLIENT_CONFIG, "FORBIDDEN_BY_CLIENT_CONFIG"}
        });
        assert data.size() == IdentifierStatus.values().length;
        return data;
    }

    @NonNull
    private final IdentifierStatus mStatus;
    @NonNull
    private final String mStringValue;

    public IdentifierStatusTest(@NonNull IdentifierStatus status, @NonNull String value) {
        mStatus = status;
        mStringValue = value;
    }

    @Test
    public void test() {
        assertThat(mStatus.getValue()).isEqualTo(mStringValue);
        assertThat(IdentifierStatus.from(mStringValue)).isEqualTo(mStatus);
    }
}
