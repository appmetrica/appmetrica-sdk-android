package io.appmetrica.analytics.impl.component;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ComponentMigrationHelperTest extends CommonTest {

    @Mock
    private ComponentUnit mComponentUnit;
    @Mock
    private ComponentId mComponentId;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    private Context mContext;
    private ComponentMigrationHelper mMigrationHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mComponentUnit.getContext()).thenReturn(mContext);
        when(mComponentUnit.getComponentId()).thenReturn(mComponentId);
        when(mComponentId.getPackage()).thenReturn("test_package");
        when(mComponentUnit.getVitalComponentDataProvider()).thenReturn(vitalComponentDataProvider);
        mMigrationHelper = new ComponentMigrationHelper.Creator().create();
    }

    @Test
    public void migrationScripts() {
        assertThat(getClasses(mMigrationHelper.getMigrationScripts())).containsExactly();
    }

    private List<Class> getClasses(@NonNull List<MigrationScript> scripts) {
        List<Class> result = new ArrayList<Class>();
        for (MigrationScript script : scripts) {
            result.add(script.getClass());
        }
        return result;
    }
}
