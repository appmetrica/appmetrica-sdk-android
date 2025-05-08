package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreapi.internal.servicecomponents.batteryinfo.ChargeType;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ChargeTypeTest extends CommonTest {

    private final Integer mId;
    private final ChargeType mChargeType;

    public ChargeTypeTest(Integer id, ChargeType chargeType) {
        mId = id;
        mChargeType = chargeType;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}] id = {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {-1, ChargeType.UNKNOWN},
            {0, ChargeType.NONE},
            {1, ChargeType.USB},
            {2, ChargeType.WIRELESS},
            {3, ChargeType.AC},
            {Integer.MIN_VALUE, ChargeType.UNKNOWN},
            {null, ChargeType.UNKNOWN}
        });
    }

    @Test
    public void testFromId() {
        assertThat(ChargeType.fromId(mId)).isEqualTo(mChargeType);
    }
}
