package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.DispatcherComponentFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public abstract class ClientUnitFactoryBaseTest extends CommonTest {

    @Mock
    protected ComponentsRepository mComponentsRepository;
    @Mock
    private ClientDescription mClientDescription;
    private final String mPackageName = "test.package.name";
    private final String mApiKey = UUID.randomUUID().toString();
    protected CommonArguments mCommonArguments;
    private Context mContext;
    private ClientUnitFactory mFactory;
    @Captor
    private ArgumentCaptor<ComponentId> mComponentIdCaptor;

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mCommonArguments = CommonArgumentsTestUtils.createMockedArguments();
        when(mClientDescription.getApiKey()).thenReturn(mApiKey);
        when(mClientDescription.getPackageName()).thenReturn(mPackageName);
        mFactory = createClientUnitFactory();
    }

    public void testCreateClientUnit(Class clientUnitClass, Class componentIdClass) {
        ClientUnit clientUnit = mFactory.createClientUnit(mContext, mComponentsRepository, mClientDescription, mCommonArguments);
        assertThat(clientUnit).isExactlyInstanceOf(clientUnitClass);
        verifyGetOrCreateComponentCall(mComponentIdCaptor);
        ComponentId componentId = mComponentIdCaptor.getValue();
        assertThat(componentId).isExactlyInstanceOf(componentIdClass);
        assertThat(componentId.getApiKey()).isEqualTo(getExpectedApiKey());
        assertThat(componentId.getPackage()).isEqualTo(mPackageName);
    }

    protected abstract ClientUnitFactory createClientUnitFactory();

    protected void verifyGetOrCreateComponentCall(ArgumentCaptor<ComponentId> captor) {
        verify(mComponentsRepository).getOrCreateRegularComponent(captor.capture(), same(mCommonArguments), any(DispatcherComponentFactory.class));
    }

    protected String getExpectedApiKey() {
        return mApiKey;
    }

}
