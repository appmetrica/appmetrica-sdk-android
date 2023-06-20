package io.appmetrica.analytics.impl.startup;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ClidsInfoStorage;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.request.CoreRequestConfig;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupConfigurationHolderTest extends CommonTest {

    private StartupRequestConfig.Arguments mConfiguration = StartupArgumentsTest.empty();
    private StartupConfigurationHolder mHolder;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        when(GlobalServiceLocator.getInstance().getClidsStorage()).thenReturn(mock(ClidsInfoStorage.class));
        mHolder = new StartupConfigurationHolder(
                new StartupRequestConfig.Loader(RuntimeEnvironment.getApplication(), RuntimeEnvironment.getApplication().getPackageName()) {

                    @Override
                    public StartupRequestConfig load(@NonNull CoreRequestConfig.CoreDataSource<StartupRequestConfig.Arguments> dataSource) {
                        return mock(StartupRequestConfig.class);
                    }
                },
                mock(StartupState.class),
                mConfiguration
        );
    }

    @Test
    public void testResetArgumentsAfterConfigurationUpdate() {
        StartupRequestConfig config = mHolder.get();
        StartupRequestConfig.Arguments arguments = mHolder.getArguments();
        mHolder.updateArguments(mConfiguration);
        assertThat(mHolder.get()).isNotSameAs(config);
        assertThat(mHolder.getArguments()).isNotSameAs(arguments);
    }

}
