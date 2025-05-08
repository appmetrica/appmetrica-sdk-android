package io.appmetrica.analytics.impl.db.storage;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CoverageUtils;
import io.appmetrica.analytics.impl.db.IKeyValueTableDbHelper;
import io.appmetrica.analytics.testutils.CommonTest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyValueTableDbHelperWrapperCoverageTest extends CommonTest {

    @Test
    public void keyValueTableDbHelperWrapperCoverage() {
        List<String> actualCoveredMethods = CoverageUtils.getTestMethodNames(KeyValueTableDbHelperWrapperTest.class);
        List<String> expectedCoveredMethods = getExpectedCoveredMethods();
        assertThat(actualCoveredMethods)
            .overridingErrorMessage("Methods of " + actualCoveredMethods +
                " do not cover the following cases: " + expectedCoveredMethods)
            .hasSameSizeAs(expectedCoveredMethods);
    }

    @NonNull
    private List<String> getExpectedCoveredMethods() {
        List<String> result = new ArrayList<String>();
        for (Method method : IKeyValueTableDbHelper.class.getDeclaredMethods()) {
            result.add(method.getName());
        }
        return result;
    }

}
