package io.appmetrica.analytics.impl.utils.validation;

import android.content.Context;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ProcessNameValidatorTest extends CommonTest {

    private static final String PACKAGE_NAME = "com.test.package.name";

    private String mInputProcessName;
    private boolean mValid;

    public ProcessNameValidatorTest(String inputProcessName, boolean valid) {
        mInputProcessName = inputProcessName;
        mValid = valid;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "[{index}] Validation result is {1} for input = \"{0}\"")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {null, false},
                {"", false},
                {PACKAGE_NAME, true},
                {PACKAGE_NAME + ":Metrica", true},
                {PACKAGE_NAME + ":passport", true},
                {PACKAGE_NAME + "_2", false},
                {PACKAGE_NAME + "_2:Metrica", false}
        });
    }

    private Context mContext;

    private ProcessNameValidator mValidator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = TestUtils.createMockedContext();
        when(mContext.getPackageName()).thenReturn(PACKAGE_NAME);
        mValidator = new ProcessNameValidator(mContext);
    }

    @Test
    public void testValidate() {
        ValidationResult result = mValidator.validate(mInputProcessName);
        assertThat(result.isValid()).isEqualTo(mValid);
        if (mValid == false) {
            assertThat(result.getDescription()).isNotEmpty();
        }
    }
}
