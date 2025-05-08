package io.appmetrica.analytics.impl.profile;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.protobuf.backend.Userprofile;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class TypesToNameMappingTest extends CommonTest {

    @ParameterizedRobolectricTestRunner.Parameters(name = "for {0} should be {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {Userprofile.Profile.Attribute.STRING, "String"},
            {Userprofile.Profile.Attribute.NUMBER, "Number"},
            {Userprofile.Profile.Attribute.COUNTER, "Counter"},
        });
    }

    private final int mAttributeType;
    private final String mTypeName;

    public TypesToNameMappingTest(int attributeType, @NonNull String typeName) {
        mAttributeType = attributeType;
        mTypeName = typeName;
    }

    @Test
    public void testMapping() {
        assertThat(TypesToNameMapping.getTypeName(mAttributeType)).isEqualTo(mTypeName);
    }

}
