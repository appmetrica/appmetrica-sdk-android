package io.appmetrica.analytics.impl.utils;

import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class DecimalProtoModelTest extends CommonTest {

    private final String input;

    public DecimalProtoModelTest(String input) {
        this.input = input;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"0"},
                {"-1"},
                {"1"},
                {"0.1"},
                {"-0.1"},
                {"10"},
                {"-10"},
                {"1000000000000000000000000"},
                {"-1000000000000000000000000"},
                {"0.00000000000000000000001"},
                {"-0.00000000000000000000001"},
                {"1000000000000000000000000000000000000000000000000001232"},
                {"-1000000000000000000000000000000000000000000000000001232"},
                {"0.00000000000000000000000000000000000000000000000000087542"},
                {"-0.00000000000000000000000000000000000000000000000000087542"},
                {"12345678901234567823353020912093021310322342931201101"},
                {"-12345678901234567823353020912093021310322342931201101"},
                {"0.12345678901234567823353020912093021310322342931201101"},
                {"-0.12345678901234567823353020912093021310322342931201101"},
                {"1200000000000000000000000056500000000000000000000000000000123343"},
                {"-1200000000000000000000000056500000000000000000000000000000123343"},
                {"0.00000000000000000000000000000000000000000012312000000000000000000"},
                {"-0.00000000000000000000000000000000000000000012312000000000000000000"},
                {String.valueOf(Long.MAX_VALUE)},
                {String.valueOf(Long.MIN_VALUE)},
                {String.valueOf(Long.MAX_VALUE - 1)},
                {String.valueOf(Long.MIN_VALUE + 1)},
                {BigInteger.valueOf(Long.MIN_VALUE).add(BigInteger.ONE.negate()).toString()},
                {BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).toString()},
                {BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN).toString()},
                {BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN).toString()},
                {BigInteger.valueOf(Long.MIN_VALUE).add(BigInteger.ONE.negate()).multiply(BigInteger.TEN).toString()},
                {BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).multiply(BigInteger.TEN).toString()},

        });
    }

    private BigDecimal inputDecimal;

    @Before
    public void setUp() throws Exception {
        inputDecimal = new BigDecimal(input);
    }

    @Test
    public void fromBigDecimal() {
        DecimalProtoModel model = DecimalProtoModel.fromDecimal(inputDecimal);
        BigDecimal fromProto = calculateFromProtoModel(model);

        assertThat(fromProto).isCloseTo(inputDecimal, withinPercentage(0.001d));
    }

    private BigDecimal calculateFromProtoModel(DecimalProtoModel model) {
        BigDecimal exponentFromProtoBasedMultiplier = new BigDecimal(10).pow(Math.abs(model.getExponent()));
        BigDecimal result = new BigDecimal(model.getMantissa());
        if (model.getExponent() > 0) {
            result = result.multiply(exponentFromProtoBasedMultiplier);
        } else {
            result = result.divide(
                    exponentFromProtoBasedMultiplier,
                    inputDecimal.scale() + 10,
                    RoundingMode.HALF_UP
            );
        }
        return result;
    }

}
