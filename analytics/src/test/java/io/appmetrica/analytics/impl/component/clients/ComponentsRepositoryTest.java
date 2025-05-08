package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponentFactory;
import io.appmetrica.analytics.impl.component.ComponentId;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponentFactory;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ComponentsRepositoryTest extends CommonTest {

    private final ComponentsRepository mComponentsRepository = new ComponentsRepository(RuntimeEnvironment.getApplication());
    @Mock
    private RegularDispatcherComponentFactory mRegularFactory;
    @Mock
    private CommutationDispatcherComponentFactory mCommutationFactory;
    private ComponentId mComponentId;
    @Mock
    private CommonArguments mConfiguration;
    @Mock
    private CommonArguments mConfiguration2;
    @Mock
    private RegularDispatcherComponent mRegularDispatcherComponent;
    @Mock
    private CommutationDispatcherComponent mCommutationDispatcherComponent;
    @Mock
    private RegularDispatcherComponent mRegularDispatcherComponent2;
    @Mock
    private CommutationDispatcherComponent mCommutationDispatcherComponent2;
    private final Context mContext = RuntimeEnvironment.getApplication();

    @Rule
    public final GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mComponentId = new ComponentId("package", UUID.randomUUID().toString());
        doReturn(mRegularDispatcherComponent).when(mRegularFactory).createDispatcherComponent(
            mContext,
            mComponentId,
            mConfiguration
        );
        doReturn(mCommutationDispatcherComponent).when(mCommutationFactory).createDispatcherComponent(
            mContext,
            mComponentId,
            mConfiguration
        );
        doReturn(mRegularDispatcherComponent2).when(mRegularFactory).createDispatcherComponent(
            mContext,
            mComponentId,
            mConfiguration2
        );
        doReturn(mCommutationDispatcherComponent2).when(mCommutationFactory).createDispatcherComponent(
            mContext,
            mComponentId,
            mConfiguration2
        );
    }

    @Test
    public void testCreateNewRegularComponent() {
        assertThat(mComponentsRepository
            .getOrCreateRegularComponent(mComponentId, mConfiguration, mRegularFactory)
        ).isEqualTo(mRegularDispatcherComponent);
    }

    @Test
    public void testCreateNewCommutationComponent() {
        assertThat(mComponentsRepository
            .getOrCreateCommutationComponent(mComponentId, mConfiguration, mCommutationFactory)
        ).isEqualTo(mCommutationDispatcherComponent);
    }

    @Test
    public void testNoNewRegularComponentCreated() {
        RegularDispatcherComponent regularDispatcherComponent = mComponentsRepository
            .getOrCreateRegularComponent(mComponentId, mConfiguration, mRegularFactory);

        assertThat(mComponentsRepository
            .getOrCreateRegularComponent(mComponentId, mConfiguration2, mRegularFactory)
        ).isEqualTo(regularDispatcherComponent);
        verify(mRegularDispatcherComponent).updateConfig(mConfiguration2);
    }

    @Test
    public void testNoNewCommutationComponentCreated() {
        CommutationDispatcherComponent commutationDispatcherComponent = mComponentsRepository
            .getOrCreateCommutationComponent(mComponentId, mConfiguration, mCommutationFactory);

        assertThat(mComponentsRepository
            .getOrCreateCommutationComponent(mComponentId, mConfiguration2, mCommutationFactory)
        ).isEqualTo(commutationDispatcherComponent);
        verify(mCommutationDispatcherComponent).updateConfig(mConfiguration2);
    }

    @Test
    public void testCommutationAfterRegular() {
        mComponentsRepository.getOrCreateRegularComponent(mComponentId, mConfiguration, mRegularFactory);
        assertThat(mComponentsRepository
            .getOrCreateCommutationComponent(mComponentId, mConfiguration, mCommutationFactory)
        ).isEqualTo(mCommutationDispatcherComponent);
    }

    @Test
    public void testRegularAfterCommutation() {
        mComponentsRepository.getOrCreateCommutationComponent(mComponentId, mConfiguration, mCommutationFactory);
        assertThat(mComponentsRepository
            .getOrCreateRegularComponent(mComponentId, mConfiguration, mRegularFactory)
        ).isEqualTo(mRegularDispatcherComponent);
    }

    @Test
    public void testGetRegularComponentIfExistsExists() {
        mComponentsRepository.getOrCreateRegularComponent(mComponentId, mConfiguration, mRegularFactory);
        assertThat(mComponentsRepository.getRegularComponentIfExists(mComponentId)).isEqualTo(mRegularDispatcherComponent);
    }

    @Test
    public void testGetRegularComponentIfExistsDoesNotExist() {
        assertThat(mComponentsRepository.getRegularComponentIfExists(mComponentId)).isNull();
    }

    @Test
    public void testGetRegularComponentIfExistsExistsOnlyCommutation() {
        mComponentsRepository.getOrCreateCommutationComponent(mComponentId, mConfiguration, mCommutationFactory);
        assertThat(mComponentsRepository.getRegularComponentIfExists(mComponentId)).isNull();
    }
}
