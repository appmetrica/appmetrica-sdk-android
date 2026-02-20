package io.appmetrica.analytics.impl.utils.validation;

import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class IntegerForRangeValidatorTest extends CommonTest {

    private final Integer mInputValue;
    private final Integer[] mPossibleValues;
    private final boolean mValid;

    private static final Integer[] ONE_ELEMENT_ARRAY = new Integer[]{100};
    private static final Integer[] EMPTY_ARRAY = new Integer[]{};
    private static final Integer[] ARRAY = new Integer[]{100, 101, 102, 110, 111, 200, 300, 400};

    public IntegerForRangeValidatorTest(Integer inputValue,
                                        Integer[] possibleValues,
                                        boolean valid,
                                        String possibleValuesString) {
        mInputValue = inputValue;
        mPossibleValues = possibleValues;
        mValid = valid;
    }

    @Parameters(name = "[{index}] Validation result is {2} for input value = {0}" +
        " and possible values = {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {100, ONE_ELEMENT_ARRAY, true, Arrays.toString(ONE_ELEMENT_ARRAY)},
            {200, ONE_ELEMENT_ARRAY, false, Arrays.toString(ONE_ELEMENT_ARRAY)},
            {100, EMPTY_ARRAY, false, Arrays.toString(EMPTY_ARRAY)},
            {102, ARRAY, true, Arrays.toString(ARRAY)},
            {103, ARRAY, false, Arrays.toString(ARRAY)},
            {null, EMPTY_ARRAY, false, Arrays.toString(EMPTY_ARRAY)},
            {null, ARRAY, false, Arrays.toString(ARRAY)}
        });
    }

    private static final String DESCRIPTION = "Description";

    private IntegerFromRangeValidator mValidator;

    @Before
    public void setUp() throws Exception {
        mValidator = new IntegerFromRangeValidator(DESCRIPTION, Arrays.asList(mPossibleValues));
    }

    @Test
    public void testValidate() {
        ValidationResult validationResult = mValidator.validate(mInputValue);
        assertThat(validationResult.isValid()).isEqualTo(mValid);
        if (!mValid) {
            assertThat(validationResult.getDescription()).contains(DESCRIPTION);
        }
    }
}
