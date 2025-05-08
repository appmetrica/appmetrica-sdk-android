package io.appmetrica.analytics.impl.db.storage;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.data.IBinaryDataHelper;
import io.appmetrica.analytics.impl.CoverageUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BinaryDataHelperWrapperCoverageTest extends CommonTest {

    @Test
    public void binaryDataHelperWrapperCoverage() {
        List<String> actualCoveredMethods = CoverageUtils.getTestMethodNames(BinaryDataHelperWrapperTest.class);
        List<String> expectedCoveredMethods = getExpectedCoveredMethods();
        assertThat(actualCoveredMethods)
            .as("Actual covered methods tested in BinaryDataHelperWrapperTest): ")
            .containsExactlyInAnyOrderElementsOf(expectedCoveredMethods);
    }

    @NonNull
    private List<String> getExpectedCoveredMethods() {
        List<String> result = new ArrayList<String>();
        for (Method method : IBinaryDataHelper.class.getDeclaredMethods()) {
            result.add(method.getName());
        }
        return result;
    }
}
