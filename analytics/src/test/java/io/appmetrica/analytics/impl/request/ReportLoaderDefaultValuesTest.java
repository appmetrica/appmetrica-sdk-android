package io.appmetrica.analytics.impl.request;

import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.impl.CertificatesFingerprintsProvider;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import io.appmetrica.analytics.testutils.rules.networktasks.NetworkServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ReportLoaderDefaultValuesTest extends CommonTest {

    private ReportRequestConfig.Loader mLoader;
    private CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> mDataSource;
    private final String mFieldName;
    private final Object mValue;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Rule
    public NetworkServiceLocatorRule networkServiceLocatorRule = new NetworkServiceLocatorRule();

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} should be equal {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"locationTracking", BuildConfig.DEFAULT_LOCATION_COLLECTING},
                {"firstActivationAsUpdate", false},
                {"sessionTimeout", 10},
                {"maxReportsCount", 7},
                {"dispatchPeriod", 90},
                {"logEnabled", false}
        });
    }

    @Before
    public void setUp() {
        ComponentUnit componentUnit = mock(ComponentUnit.class);
        when(componentUnit.getContext()).thenReturn(RuntimeEnvironment.getApplication());
        when(componentUnit.getComponentId()).thenReturn(mock(ComponentId.class));
        when(componentUnit.getCertificatesFingerprintsProvider()).thenReturn(mock(CertificatesFingerprintsProvider.class));
        when(componentUnit.getVitalComponentDataProvider()).thenReturn(mock(VitalComponentDataProvider.class));

        ClidsInfoStorage clidsStorage = GlobalServiceLocator.getInstance().getClidsStorage();
        when(clidsStorage.updateAndRetrieveData(any(ClidsInfo.Candidate.class)))
                .thenReturn(new ClidsInfo.Candidate(null, DistributionSource.APP));

        mLoader = new ReportRequestConfig.Loader(componentUnit, mock(ReportRequestConfig.StatisticsSendingStrategy.class));
        mDataSource = new CoreRequestConfig.CoreDataSource<>(
                TestUtils.createDefaultStartupState(),
                ReportArgumentsTest.createEmptyArguments()
        );
    }

    public ReportLoaderDefaultValuesTest(String fieldName, Object value) {
        mFieldName = fieldName;
        mValue = value;
    }

    @Test
    public void testSimple() {
        assertThat(mLoader.load(mDataSource)).extracting(mFieldName).isEqualTo(mValue);
    }

}
