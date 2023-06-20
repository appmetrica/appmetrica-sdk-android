package io.appmetrica.analytics.impl.request;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.LocaleHolder;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.networktasks.internal.BaseRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ConfigurationHolderTest extends CommonTest {

    private final BaseRequestConfig.RequestConfigLoader<BaseRequestConfig, CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>> mLoader = new BaseRequestConfig.RequestConfigLoader<BaseRequestConfig, CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>>() {
        @NonNull
        @Override
        public BaseRequestConfig load(CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments> dataSource) {
            return mock(BaseRequestConfig.class);
        }
    };
    private ConfigurationHolder<BaseRequestConfig, CommonArguments.ReporterArguments, ReportRequestConfig.Arguments, BaseRequestConfig.RequestConfigLoader<BaseRequestConfig, CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>>> mHolder;

    @Rule
    public final GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();
    @Rule
    public final MockedStaticRule<LocaleHolder> sLocaleHolder = new MockedStaticRule<>(LocaleHolder.class);
    @Mock
    private LocaleHolder localeHolder;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(LocaleHolder.getInstance(any(Context.class))).thenReturn(localeHolder);
        mHolder =  new ConfigurationHolder<BaseRequestConfig, CommonArguments.ReporterArguments, ReportRequestConfig.Arguments, BaseRequestConfig.RequestConfigLoader<BaseRequestConfig, CoreRequestConfig.CoreDataSource<ReportRequestConfig.Arguments>>>(
                mLoader,
                mock(StartupState.class),
                ReportRequestConfig.Arguments.empty()
        ) {};
    }

    @Test
    public void testNoNewConfig() {
        assertThat(mHolder.get()).isSameAs(mHolder.get());
    }

    @Test
    public void testReset() {
        BaseRequestConfig config1 = mHolder.get();
        mHolder.reset();
        assertThat(mHolder.get()).isNotSameAs(config1);
    }

    @Test
    public void testUpdateStartupState() {
        StartupState startupState = mock(StartupState.class);
        assertThat(mHolder.getStartupState()).isNotSameAs(startupState);
        mHolder.updateStartupState(startupState);
        assertThat(mHolder.getStartupState()).isSameAs(startupState);
    }

    @Test
    public void testResetConfigOnNewStartupState() {
        BaseRequestConfig config1 = mHolder.get();
        mHolder.updateStartupState(mock(StartupState.class));
        assertThat(mHolder.get()).isNotSameAs(config1);
    }

    @Test
    public void testUpdateLocale() {
        BaseRequestConfig config1 = mHolder.get();
        verify(localeHolder).registerLocaleUpdatedListener(mHolder);
        mHolder.onLocalesUpdated();
        assertThat(mHolder.get()).isNotSameAs(config1);
    }
}
